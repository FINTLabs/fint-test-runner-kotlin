package no.fintlabs.testrunner.resource

import org.springframework.stereotype.Repository

@Repository
class ResourceRepository {

    val resources: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun getResources(endpoint: String): MutableList<String>? = resources[createComponentKey(endpoint)]

    fun createComponentKey(endpoint: String) = endpoint.split("/").let { "${it[1]}.${it[2]}" }

}