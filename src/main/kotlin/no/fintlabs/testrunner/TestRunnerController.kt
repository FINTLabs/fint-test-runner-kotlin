package no.fintlabs.testrunner

import jakarta.validation.Valid
import no.fintlabs.testrunner.model.TestRequest
import no.fintlabs.testrunner.model.TestResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{orgName}/run")
class TestRunnerController(
    val testRunnerService: TestRunnerService
) {

    @PostMapping
    suspend fun runTest(@PathVariable orgName: String, @Valid @RequestBody testRequest: TestRequest): ResponseEntity<TestResult> =
        ResponseEntity.ok(testRunnerService.run(orgName, testRequest))

}