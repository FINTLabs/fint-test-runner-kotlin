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

    private val log = LoggerFactory.getLogger(HealthTestService::class.java)

    suspend fun run(orgName: String, testRequest: TestRequest): HealthTestResult {
        return try {
            log.debug("Request: ${testRequest.baseUrl}${testRequest.endpoint}")
            val response = fintApiService.getHealthStatusResponse(
                testRequest.baseUrl,
                testRequest.endpoint,
                orgName,
                testRequest.clientName
            )
            log.debug("Response: {}", response)
            val responseBody = response.body ?: throw IllegalStateException("No response: ${response.statusCode}")
            HealthTestResult.ofEventHealth(responseBody)
        } catch (exception: RestClientException) {
            HealthTestResult(
                exception.message,
                Status.FAILED,
            )
        }
    }

}