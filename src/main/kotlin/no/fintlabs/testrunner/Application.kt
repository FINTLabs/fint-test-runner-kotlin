package no.fintlabs.testrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FintTestRunnerKotlinApplication

fun main(args: Array<String>) {
	runApplication<FintTestRunnerKotlinApplication>(*args)
}
