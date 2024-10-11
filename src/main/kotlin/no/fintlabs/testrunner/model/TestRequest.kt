package no.fintlabs.testrunner.model

data class TestRequest(
    val baseUrl: String,
    val endpoint: String,
    val clientName: String
)
