package me.hama

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestApiProducer

fun main(args: Array<String>) {
    runApplication<RestApiProducer>(*args)
}
