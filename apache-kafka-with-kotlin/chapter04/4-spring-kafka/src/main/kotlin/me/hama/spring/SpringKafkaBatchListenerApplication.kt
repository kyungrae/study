package me.hama.spring

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.KafkaListener

private val logger: Logger = LoggerFactory.getLogger(SpringKafkaBatchListenerApplication::class.java)

@SpringBootApplication
class SpringKafkaBatchListenerApplication {

    @KafkaListener(topics = ["test"], groupId = "test-group-00")
    fun batchListener(records: ConsumerRecords<String, String>) {
        records.forEach { record -> logger.info(record.toString()) }
    }

    @KafkaListener(topics = ["test"], groupId = "test-group-01")
    fun batchListener(messages: List<String>) {
        messages.forEach { message -> logger.info(message) }
    }

    @KafkaListener(topics = ["test"], groupId = "test-group-02", concurrency = "3")
    fun concurrentBatchListener(records: ConsumerRecords<String, String>) {
        records.forEach { record -> logger.info(record.toString()) }
    }
}

fun main(args: Array<String>) {
    runApplication<SpringKafkaBatchListenerApplication>(*args)
}
