package com.rn00n.inhibitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
@ConfigurationPropertiesScan
class InhibitorApplication

fun main(args: Array<String>) {
    runApplication<InhibitorApplication>(*args)
}
