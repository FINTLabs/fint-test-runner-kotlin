package no.fintlabs.testrunner

import kotlinx.coroutines.reactor.awaitSingle
import no.fint.event.model.Event
import no.fint.event.model.health.Health
import no.fintlabs.testrunner.auth.AuthService
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

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
        val headers = createAuthorizationHeader(baseUrl, orgName, clientName)
        println("Requesting to url: $baseUrl$endpoint/admin/health \nWith headers: $headers")
        return webClient.get()
            .uri("$baseUrl$endpoint/admin/health")
            .headers { it.addAll(headers) }
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Event<Health>>() {})
            .awaitSingle()
    }

    private suspend fun getLong(
        baseUrl: String,
        endpoint: String,
        mapKey: String,
        orgName: String,
        clientName: String
    ): Long {
        val headers = createAuthorizationHeader(baseUrl, orgName, clientName)
        println("Requesting to url: ${baseUrl}${endpoint} \nWith headers: $headers")
        return try {
            val responseMap = webClient.get()
                .uri("${baseUrl}${endpoint}")
                .headers { it.addAll(headers) }
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .awaitSingle()

            println("Body: $responseMap")
            val desiredValue = responseMap[mapKey] as? Number

            if (desiredValue != null) {
                desiredValue.toLong()
            } else {
                println("Key '$mapKey' not found or value is not a number.")
                -1
            }
        } catch (e: Exception) {
            println("ERROR OCCURRED: ${e.message}")
            -1
        }
    }

    private suspend fun createAuthorizationHeader(baseUrl: String, orgName: String, clientName: String): HttpHeaders =
        HttpHeaders().apply {
            if (baseUrl.contains("play-with-fint")) {
                set("x-org-id", "pwf.no")
                set("x-client", "pwf_no_client")
            } else {
                val accessToken = authService.getNewAccessToken(orgName, clientName)
                set("Authorization", "Bearer $accessToken")
            }
        }

}