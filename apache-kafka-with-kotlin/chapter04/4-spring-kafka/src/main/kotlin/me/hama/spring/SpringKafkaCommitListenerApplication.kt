package me.hama.spring

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment

private val logger: Logger = LoggerFactory.getLogger(SpringKafkaCommitListenerApplication::class.java)

@SpringBootApplication
class SpringKafkaCommitListenerApplication {

    @KafkaListener(topics = ["test"], groupId = "test-group-01")
    fun commitListener(records: ConsumerRecords<String, String>, ack: Acknowledgment) {
        records.forEach { record -> logger.info(record.toString()) }
        ack.acknowledge()
    }

    @KafkaListener(topics = ["test"], groupId = "test-group-02")
    fun concurrentTopicListener(records: ConsumerRecords<String, String>, consumer: Consumer<String, String>) {
        records.forEach { record -> logger.info(record.toString()) }
        consumer.commitAsync()
    }
}

fun main(args: Array<String>) {
    runApplication<SpringKafkaCommitListenerApplication>(*args)
}
