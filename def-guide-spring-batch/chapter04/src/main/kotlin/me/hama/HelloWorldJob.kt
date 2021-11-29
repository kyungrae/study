package me.hama

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableBatchProcessing
@SpringBootApplication
class HelloWorldJob {
}

fun main(args: Array<String>) {
    runApplication<HelloWorldJob>(*args)
}
