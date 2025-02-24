package no.fintlabs.testrunner

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
            emptySet(),
            "Sorry but we can't find the service: ${testRequest.baseUrl}${testRequest.endpoint}"
        )

    private suspend fun createTestResult(orgName: String, testRequest: TestRequest, resources: MutableList<String>): TestResult =
        coroutineScope {
            println("Creating reslut for test ${testRequest}")
            val resourceResults = resources.map { resource ->
                async {
                    val resourceResult = ResourceResult(
                        resource,
                        fintApiService.getLastUpdated(testRequest.baseUrl, "${testRequest.endpoint}/$resource", orgName, testRequest.clientName),
                        fintApiService.getCacheSize(testRequest.baseUrl, "${testRequest.endpoint}/$resource", orgName, testRequest.clientName)
                    )
                    resourceResult.generateStatus()
                    resourceResult
                }
            }.awaitAll()

            TestResult(resourceResults.toSet())
        }

}