package no.fintlabs.testrunner.auth

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.fintlabs.testrunner.auth.model.AuthObject
import no.fintlabs.testrunner.auth.model.AuthResponse
import no.fintlabs.testrunner.auth.model.TokenResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.ConcurrentHashMap

@Service
class AuthService(
    val gatewayWebClient: WebClient,
    val idpWebClient: WebClient
) {

    private val mutexCache = ConcurrentHashMap<String, Mutex>()

    suspend fun getNewAccessToken(orgName: String, clientName: String): String {
        val cacheKey = "$orgName|$clientName"

        val mutex = mutexCache.computeIfAbsent(cacheKey) { Mutex() }
        return mutex.withLock {
            val newToken = fetchNewAccessToken(orgName, clientName)
            newToken.accessToken
        }
    }

    private suspend fun fetchNewAccessToken(orgName: String, clientName: String): TokenResponse {
        val clientNameLowercase = clientName.lowercase()
        val authResponse = getAuthResponse(orgName, clientNameLowercase)
        val resetAuthResponse = resetAuthResponse(clientNameLowercase, authResponse)
        val decryptedAuthObject = decryptAuthResponse(clientNameLowercase, resetAuthResponse)
        val tokenResponse = getTokenResponse(decryptedAuthObject)
        tokenResponse.createdAt = System.currentTimeMillis() / 1000
        return tokenResponse
    }

    private suspend fun resetAuthResponse(clientName: String, authResponse: AuthResponse): AuthResponse =
        gatewayWebClient.post()
            .uri(createResetUri(clientName))
            .bodyValue(authResponse)
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .awaitSingle()

    private fun createResetUri(clientName: String): String {
        return when {
            clientName.contains("@adapter") -> "/adapter/password/reset"
            else -> "/client/password/reset"
        }
    }

    private suspend fun getTokenResponse(decryptedAuthObject: AuthObject): TokenResponse {
        val createFormData = createFormData(decryptedAuthObject)
        return idpWebClient.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(createFormData))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .awaitSingle()
    }

    private fun createFormData(authObject: AuthObject): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("client_id", authObject.clientId)
            add("client_secret", authObject.clientSecret)
            add("username", authObject.name)
            add("password", authObject.password)
            add("scope", createScope(authObject.name))
        }

    private fun createScope(name: String): String {
        return when {
            name.contains("@adapter") -> "fint-adapter"
            else -> "fint-client"
        }
    }

    private suspend fun decryptAuthResponse(clientName: String, authResponse: AuthResponse) =
        gatewayWebClient.post()
            .uri(createDecryptUri(clientName))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(authResponse)
            .retrieve()
            .bodyToMono(AuthObject::class.java)
            .awaitSingle()

    private suspend fun getAuthResponse(orgName: String, clientName: String) =
        gatewayWebClient.get()
            .uri(createDnUri(orgName, clientName))
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .awaitSingle()

    private fun createDnUri(orgName: String, clientName: String): String {
        val formattedOrgName = orgName.replace(".", "_")
        return when {
            clientName.contains("@adapter") ->
                "/adapter/cn=$clientName,ou=adapters,ou=$formattedOrgName,ou=organisations,o=fint"
            else ->
                "/client/cn=$clientName,ou=clients,ou=$formattedOrgName,ou=organisations,o=fint"
        }
    }

    private fun createDecryptUri(clientName: String): String {
        return when {
            clientName.contains("@adapter") -> "/adapter/decrypt"
            else -> "/client/decrypt"
        }
    }
}
