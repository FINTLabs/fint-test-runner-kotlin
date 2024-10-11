package no.fintlabs.testrunner.health

import no.fintlabs.testrunner.model.TestRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{orgName}/health")
class HealthTestController(
    val healthTestService: HealthTestService
) {

    @PostMapping
    suspend fun healthTest(@PathVariable orgName: String, @RequestBody testRequest: TestRequest) =
        ResponseEntity.ok(healthTestService.run(orgName, testRequest))

}