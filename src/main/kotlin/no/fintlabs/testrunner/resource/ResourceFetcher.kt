package no.fintlabs.testrunner.resource

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ResourceFetcher(
    resourceRepository: ResourceRepository,
    metaModelWebClient: WebClient
) {

    init {
        metaModelWebClient.get()
            .retrieve()
            .bodyToFlux(Metadata::class.java)
            .subscribe { metadata ->
                metadata.packageName?.let {
                    val componentKey = "${metadata.domainName}.${metadata.packageName}"
                    resourceRepository.resources.getOrPut(componentKey) { mutableListOf() }
                        .add(metadata.resourceName)
                }
            }
    }

}