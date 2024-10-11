package no.fintlabs.testrunner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${fint.metamodel-url}")
    lateinit var metamodelUrl: String

    @Value("\${fint.gateway-url}")
    lateinit  var gatewayUrl: String

    @Value("\${fint.idp-url}")
    lateinit  var idpUrl: String

    @Bean
    fun idpWebClient(): WebClient = WebClient.builder().baseUrl(idpUrl).build()

    @Bean
    fun gatewayWebClient(): WebClient = WebClient.builder().baseUrl(gatewayUrl).build()

    @Bean
    fun metaModelWebClient(): WebClient = WebClient.builder().baseUrl(metamodelUrl).build()

    @Bean
    fun webClient(): WebClient = WebClient.create()

}