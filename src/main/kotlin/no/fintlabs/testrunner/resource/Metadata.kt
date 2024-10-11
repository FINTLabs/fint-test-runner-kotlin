package no.fintlabs.testrunner.resource

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Metadata(
    val domainName: String,
    val packageName: String?,
    val resourceName: String,
)
