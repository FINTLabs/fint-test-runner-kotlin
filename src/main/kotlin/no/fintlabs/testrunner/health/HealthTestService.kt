package no.fintlabs.testrunner.health

import no.fintlabs.testrunner.FintApiService
import no.fintlabs.testrunner.model.Status
import no.fintlabs.testrunner.model.TestRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException

@Service
class HealthTestService(
    val fintApiService: FintApiService
) {

    suspend fun run(orgName: String, testRequest: TestRequest): HealthTestResult {
        return try {
            HealthTestResult.ofEventHealth(
                fintApiService.getHealthEvent(
                    testRequest.baseUrl,
                    testRequest.endpoint,
                    orgName,
                    testRequest.clientName
                )
            )
        } catch (exception: RestClientException) {
            HealthTestResult(
                exception.message,
                Status.FAILED,
            )
        }
    }

}