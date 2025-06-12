package org.acme

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "quarkus.mongodb")
interface MongoClientConfig {
    fun connectionString(): String
    fun database(): String
}