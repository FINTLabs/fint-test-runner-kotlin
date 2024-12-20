package no.fintlabs.testrunner.model

import java.time.Duration
import java.time.Instant

data class ResourceResult(
    val resource: String,
    val lastUpdated: Long,
    val size: Long,
    var status: Status = Status.NOT_RUNNED,
    var message: String = "",
) {

    fun generateStatus() {
        if (lastUpdated.toInt() == -1 && size.toInt() == -1) {
            status = Status.FAILED
            message = "We had problems connecting to the endpoints."
        } else if (lastUpdated == -403L) {
            status = Status.PARTIALLY_FAILED
            message = "Client does not have access to this component"
        } else if (lastUpdated.toInt() == 0 && size.toInt() == 0) {
            status = Status.FAILED
            message = "Cache has never been updated."
        } else if (lastUpdated > 0 && size.toInt() == 0) {
            status = Status.PARTIALLY_FAILED
            message = "Cache have been updated, but cache is empty."
        } else if (isLastUpdatedToOld()) {
            status = Status.PARTIALLY_FAILED
            message = "Cache have not been updated in ${30} minutes"
        } else {
            status = Status.OK
        }
    }

    private fun isLastUpdatedToOld(): Boolean {
        val now = Instant.now()
        val resourceLastUpdated = Instant.ofEpochMilli(lastUpdated)
        val duration = Duration.between(resourceLastUpdated, now)
        val limit = Duration.ofMinutes(30)
        return duration > limit
    }

}
