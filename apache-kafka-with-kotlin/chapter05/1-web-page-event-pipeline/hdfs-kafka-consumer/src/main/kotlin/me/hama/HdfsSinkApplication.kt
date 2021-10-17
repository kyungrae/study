package me.hama

import me.hama.consumer.ConsumerWorker
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors

private val logger: Logger = LoggerFactory.getLogger("HdfsSinkApplication")
private var workers = mutableListOf<ConsumerWorker>()

fun main() {
    Runtime.getRuntime().addShutdownHook(ShutdownThread())
    val configs = Properties()
    configs[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    configs[ConsumerConfig.GROUP_ID_CONFIG] = "select-color"
    configs[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    configs[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

    val executor = Executors.newCachedThreadPool()
    workers = MutableList(3) { ConsumerWorker(configs, "select-color", it) }
    workers.forEach(executor::execute)
}

class ShutdownThread : Thread() {
    override fun run() {
        logger.info("Shutdown hook")
        workers.forEach(ConsumerWorker::stopAndWakeup)
    }
}
