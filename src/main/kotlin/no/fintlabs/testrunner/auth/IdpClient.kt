package no.fintlabs.testrunner.auth

import kotlinx.coroutines.reactor.awaitSingle
import no.fintlabs.testrunner.auth.model.AuthObject
import no.fintlabs.testrunner.auth.model.TokenResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component
class IdpClient(
    @Qualifier("idpWebClient")
    private val client: WebClient
) {

    suspend fun getTokenResponse(decryptedAuthObject: AuthObject): TokenResponse =
        client.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(createFormData(decryptedAuthObject)))
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
            add("scope", "fint-client")
        }

}