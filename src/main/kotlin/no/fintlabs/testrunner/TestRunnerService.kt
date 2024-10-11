package no.fintlabs.testrunner

import no.fintlabs.testrunner.resource.ResourceRepository
import no.fintlabs.testrunner.model.ResourceResult
import no.fintlabs.testrunner.model.TestRequest
import no.fintlabs.testrunner.model.TestResult
import org.springframework.stereotype.Service

@Service
class TestRunnerService(
    val resourceRepository: ResourceRepository,
    val fintApiService: FintApiService
) {

    suspend fun run(orgName: String, testRequest: TestRequest): TestResult =
        resourceRepository.getResources(testRequest.endpoint)?.let { resources ->
            createTestResult(orgName, testRequest, resources)
        } ?: TestResult(
            emptyList(),
            "Sorry but we can't find the service: ${testRequest.baseUrl}${testRequest.endpoint}"
        )

    private suspend fun createTestResult(
        orgName: String,
        testRequest: TestRequest,
        resources: MutableList<String>
    ): TestResult =
        TestResult(
            resources.map { resource ->
                val resourceResult = ResourceResult(
                    resource = resource,
                    lastUpdated = fintApiService.getLastUpdated(testRequest.baseUrl, testRequest.endpoint, orgName, testRequest.clientName),
                    size = fintApiService.getCacheSize(testRequest.baseUrl, testRequest.endpoint, orgName, testRequest.clientName)
                )
                resourceResult.generateStatus()
                resourceResult
            }
        )

}