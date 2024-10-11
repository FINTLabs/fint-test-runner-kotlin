package no.fintlabs.testrunner.health

import no.fint.event.model.Event
import no.fint.event.model.health.Health
import no.fint.event.model.health.HealthStatus
import no.fintlabs.testrunner.model.Status

data class HealthTestResult(
    val message: String?,
    val status: Status = Status.NOT_RUNNED,
    val healthData: MutableList<Health> = mutableListOf()
) {
    companion object {
        fun ofEventHealth(healthEvent: Event<Health>): HealthTestResult {
            val healthData = healthEvent.data.toMutableList()
            val message = healthEvent.message ?: "No message"

            val healthy = healthData.any { it.status == HealthStatus.APPLICATION_HEALTHY.toString() }
            val unhealthy = healthData.any { it.status == HealthStatus.APPLICATION_UNHEALTHY.toString() }

            val status = when {
                healthy -> Status.OK
                unhealthy -> Status.PARTIALLY_FAILED
                else -> Status.FAILED
            }

            return HealthTestResult(message, status, healthData)
        }
    }
}
