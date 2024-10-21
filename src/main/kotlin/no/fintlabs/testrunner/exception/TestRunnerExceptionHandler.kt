package no.fintlabs.testrunner.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TestRunnerExceptionHandler {

    @ExceptionHandler(InvalidAuthenticationException::class)
    fun handleInvalidAuthenticationException(e: InvalidAuthenticationException) =
        ResponseEntity.badRequest().body("clientName field cant be empty or null if the request is not to play-with-fint")

}