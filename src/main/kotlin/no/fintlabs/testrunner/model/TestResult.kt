package no.fintlabs.testrunner.model

data class TestResult(
    val resourceResults: List<ResourceResult>,
    val message: String = ""
)
