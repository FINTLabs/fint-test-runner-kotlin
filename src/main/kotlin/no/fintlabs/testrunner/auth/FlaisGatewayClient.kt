package no.fintlabs.testrunner.auth

import kotlinx.coroutines.reactor.awaitSingle
import no.fintlabs.testrunner.auth.model.AuthObject
import no.fintlabs.testrunner.auth.model.AuthResponse
import no.fintlabs.testrunner.auth.model.ClientRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class FlaisGatewayClient(
    @Qualifier("gatewayWebClient")
    val client: WebClient,
) {

    suspend fun createClient(orgId: String): AuthResponse? =
        client.post()
            .uri("/client")
            .bodyValue(ClientRequest(orgId = orgId))
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .awaitSingle()

    suspend fun decryptAuthResponse(authResponse: AuthResponse): AuthObject =
        client.post()
            .uri("/client/decrypt")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(authResponse)
            .retrieve()
            .bodyToMono(AuthObject::class.java)
            .awaitSingle()

    suspend fun getEncryptedAuthResponse(orgId: String, clientName: String): AuthResponse? =
        client.get()
            .uri(createDnUri(orgId, clientName))
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .awaitSingle()
            .takeIf(::clientExists)

    private fun clientExists(authResponse: AuthResponse) = authResponse.authObject != null

    private fun createDnUri(orgId: String, clientName: String): String =
        "/client/cn=$clientName,ou=clients,ou=${formatOrgId(orgId)},ou=organisations,o=fint"

    private fun formatOrgId(orgId: String) =
        orgId.replace(".", "_")
            .replace("-", "_")

}