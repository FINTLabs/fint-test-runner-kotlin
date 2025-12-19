package no.fintlabs.testrunner.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.fintlabs.testrunner.auth.model.TokenResponse
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class AuthService(
    private val idpClient: IdpClient,
    private val flaisGatewayClient: FlaisGatewayClient
) {

    private val tokenCache = ConcurrentHashMap<String, TokenResponse>()
    private val mutexCache = ConcurrentHashMap<String, Mutex>()

    suspend fun getNewAccessToken(orgId: String): String {
        val cachedToken = tokenCache[orgId]

        if (cachedToken != null && !tokenExpired(cachedToken)) {
            return cachedToken.accessToken
        } else {
            val mutex = mutexCache.computeIfAbsent(orgId) { Mutex() }
            return mutex.withLock {
                val doubleCheckedToken = tokenCache[orgId]
                if (doubleCheckedToken != null && !tokenExpired(doubleCheckedToken)) {
                    doubleCheckedToken.accessToken
                } else {
                    val newToken = fetchNewAccessToken(orgId)
                    newToken.createdAt = System.currentTimeMillis() / 1000
                    tokenCache[orgId] = newToken
                    newToken.accessToken
                }
            }
        }
    }

    private fun tokenExpired(tokenResponse: TokenResponse): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        val tokenCreationTime = tokenResponse.createdAt
        val expiresIn = tokenResponse.expiresIn

        return (currentTime - tokenCreationTime) >= expiresIn
    }

    private suspend fun fetchNewAccessToken(orgId: String): TokenResponse =
        flaisGatewayClient.getEncryptedAuthResponse(orgId)
            ?.let { flaisGatewayClient.decryptAuthResponse(it) }
            ?.let { idpClient.getTokenResponse(it) }
            ?: flaisGatewayClient.createClient(orgId).let { fetchNewAccessToken(orgId) }

}