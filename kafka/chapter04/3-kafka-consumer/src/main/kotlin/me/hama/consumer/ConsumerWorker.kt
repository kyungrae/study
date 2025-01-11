package me.hama.consumer

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlin.math.log

private val logger: Logger = LoggerFactory.getLogger(ConsumerWorker::class.java)

class ConsumerWorker(
    private val recordValue: String
) : Runnable {
    override fun run() {
        logger.info("thread:${Thread.currentThread().name}\trecord:$recordValue");
    }
}
