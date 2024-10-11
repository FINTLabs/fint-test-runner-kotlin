package no.fintlabs.testrunner.auth.model

data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val acr: String,
    val scope: String
)
