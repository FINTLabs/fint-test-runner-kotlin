package no.fintlabs.testrunner

import io.netty.util.internal.StringUtil
import kotlinx.coroutines.reactor.awaitSingle
import no.fint.event.model.Event
import no.fint.event.model.health.Health
import no.fintlabs.testrunner.auth.AuthService
import no.fintlabs.testrunner.exception.InvalidAuthenticationException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import kotlin.math.log

@Service
class FintApiService(
    val authService: AuthService,
    val webClient: WebClient
) {

    suspend fun getLastUpdated(baseUrl: String, endpoint: String, orgName: String, clientName: String): Long =
        getLong(baseUrl, "${endpoint}/last-updated", "lastUpdated", orgName, clientName)

    suspend fun getCacheSize(baseUrl: String, endpoint: String, orgName: String, clientName: String): Long =
        getLong(baseUrl, "${endpoint}/cache/size", "size", orgName, clientName)

    suspend fun getHealthEvent(baseUrl: String, endpoint: String, orgName: String, clientName: String): Event<Health> {
        try {
            val headers = createAuthorizationHeader(baseUrl, orgName, clientName)
            return webClient.get()
                .uri("$baseUrl$endpoint/admin/health")
                .headers { it.addAll(headers) }
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<Event<Health>>() {})
                .awaitSingle()
        } catch (e: Exception) {
            println("Failed to fetch admin health: ${e.stackTrace}")
            throw e
        }
    }

    private suspend fun getLong(
        baseUrl: String,
        endpoint: String,
        mapKey: String,
        orgName: String,
        clientName: String
    ): Long {
        val headers = createAuthorizationHeader(baseUrl, orgName, clientName)
        return try {
            val responseMap = webClient.get()
                .uri("$baseUrl$endpoint")
                .headers { it.addAll(headers) }
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .awaitSingle()

            (responseMap[mapKey] as? Number)?.toLong()
                ?: (responseMap[mapKey] as? String)?.toLongOrNull()
                ?: -1L
        } catch (e: WebClientResponseException){
            if (e.statusCode.equals(HttpStatus.FORBIDDEN))
                println(e.stackTrace)
                return -403L
        } catch (e: Exception){
            print(e.stackTrace)
            return -1L
        }
    }

    private suspend fun createAuthorizationHeader(baseUrl: String, orgName: String, clientName: String): HttpHeaders =
        HttpHeaders().apply {
            if (baseUrl.contains("play-with-fint")) {
                set("x-org-id", "pwf.no")
                set("x-client", "pwf_no_client")
            } else {
                if (StringUtil.isNullOrEmpty(clientName))
                    throw InvalidAuthenticationException(clientName)
                set("Authorization", "Bearer ${authService.getNewAccessToken(orgName, clientName)}")
            }
        }

}