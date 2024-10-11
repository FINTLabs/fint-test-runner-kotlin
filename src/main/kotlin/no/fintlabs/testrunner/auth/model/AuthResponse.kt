package no.fintlabs.testrunner.auth.model

import no.fintlabs.testrunner.auth.model.AuthObject

data class AuthResponse(
    val `object`: AuthObject,
    val orgId: String,
    val operation: String
)
