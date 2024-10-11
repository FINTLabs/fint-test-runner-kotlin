package no.fintlabs.testrunner.auth

import kotlinx.coroutines.reactor.awaitSingle
import no.fintlabs.testrunner.auth.model.AuthObject
import no.fintlabs.testrunner.auth.model.AuthResponse
import no.fintlabs.testrunner.auth.model.TokenResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Service
class AuthService(
    val gatewayWebClient: WebClient,
    val idpWebClient: WebClient
) {

    suspend fun getNewAccessToken(orgName: String, clientName: String): String {
        val decryptAuthResponse = decryptAuthResponse(clientName, getAuthResponse(orgName, clientName))
        return getTokenResponse(decryptAuthResponse).accessToken
    }

    private suspend fun getTokenResponse(decryptAuthResponse: AuthResponse): TokenResponse =
        idpWebClient.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(createFormData(decryptAuthResponse.`object`)))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .awaitSingle()


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
            .bodyValue(authResponse)
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .awaitSingle()


    private suspend fun getAuthResponse(orgName: String, clientName: String) =
        gatewayWebClient.get()
            .uri(createUri(orgName, clientName))
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .awaitSingle()


    private fun createUri(orgName: String, clientName: String): String {
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
            clientName.contains("@adapter") ->
                "/adapter/decrypt"
            else ->
                "/client/decrypt"
        }
    }

}