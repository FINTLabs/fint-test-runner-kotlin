package no.fintlabs.testrunner.resource

import org.springframework.stereotype.Repository

@Repository
class ResourceRepository {

    val resources: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun getResources(endpoint: String): MutableList<String>? {
        return resources[createComponentKey(endpoint)]
    }

    fun createComponentKey(endpoint: String) = endpoint.split("/").let { "${it[0]}.${it[1]}" }

}