package no.fintlabs.testrunner.health

import no.fintlabs.testrunner.FintApiService
import no.fintlabs.testrunner.model.Status
import no.fintlabs.testrunner.model.TestRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.reactive.function.client.WebClientResponseException

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
        } catch (webClientResponseException: WebClientResponseException) {
            if (webClientResponseException.statusCode == HttpStatus.FORBIDDEN) {
                return HealthTestResult(
                    "Client does not have access to component",
                    Status.FAILED,
                )
            }
            throw webClientResponseException
        }
    }

}