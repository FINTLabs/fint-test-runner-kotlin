package no.fintlabs.testrunner.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern


data class TestRequest(
    @field:NotBlank(message = "baseUrl must not be empty")
    @field:Pattern(
        regexp = "^https://[A-Za-z0-9\\-]+\\.felleskomponent\\.no",
        message = "baseUrl must start with 'https://(string or -).felleskomponent.no'"
    )
    val baseUrl: String,

    @field:NotBlank(message = "endpoint must not be empty")
    @field:Pattern(
        regexp = "^/([a-zA-Z]+)(/[a-zA-Z]+)*\$",
        message = "endpoint must start with '/' and contain valid path segments without a trailing slash"
    )
    val endpoint: String,

    @field:NotBlank(message = "clientName must not be empty")
    @field:Pattern(
        regexp = "^.+@client\\.[^.]+\\.no\$",
        message = "clientName must match '*@client.*.no'"
    )
    val clientName: String
)
