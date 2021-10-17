package me.hama.spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import kotlin.system.exitProcess

@SpringBootApplication
class SpringProducerApplication : ApplicationRunner {

    @Autowired
    private lateinit var customKafkaTemplate: KafkaTemplate<String, String>

    override fun run(args: ApplicationArguments) {
        val future = customKafkaTemplate.send(TOPIC_NAME, "test")
        future.addCallback(
            { result: SendResult<String, String> -> println(result) },
            { e: Throwable -> println(e) }
        )
        exitProcess(0)
    }

    companion object {
        const val TOPIC_NAME = "test"
    }
}

fun main(args: Array<String>) {
    runApplication<SpringProducerApplication>(*args)
}
