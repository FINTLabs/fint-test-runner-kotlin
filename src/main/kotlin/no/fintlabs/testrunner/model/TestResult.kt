package no.fintlabs.testrunner.model

data class TestResult(
    val resourceResults: Set<ResourceResult>,
    val message: String = ""
)
