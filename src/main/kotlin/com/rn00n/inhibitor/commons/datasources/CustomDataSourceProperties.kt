package com.rn00n.inhibitor.commons.datasources

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "datasources")
data class CustomDataSourceProperties(
    val databases: MutableMap<String, MutableMap<String, DataSourceConfig>> = mutableMapOf()
)

data class DataSourceConfig(
    val url: String,
    val username: String,
    val password: String,
    val driverClassName: String = "com.mysql.cj.jdbc.Driver",
    val replicaUrl: String? = null,
    val maxPoolSize: Int = 30,
    val minIdle: Int = 5,
    val ddlAuto: String = "validate"
)