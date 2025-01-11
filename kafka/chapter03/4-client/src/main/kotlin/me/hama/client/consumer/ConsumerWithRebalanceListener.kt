package me.hama.client.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

fun main() {
    val configs = Properties()
    configs[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    configs[ConsumerConfig.GROUP_ID_CONFIG] = GROUP_ID
    configs[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    configs[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    configs[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false

    val consumer = KafkaConsumer<String, String>(configs)
    val currentOffset = mutableMapOf<TopicPartition, OffsetAndMetadata>()
    val rebalanceListener = RebalanceListener(consumer, currentOffset)
    consumer.subscribe(listOf(TOPIC_NAME), rebalanceListener)

    while (true) {
        val records = consumer.poll(Duration.ofSeconds(1))
        for (record in records) {
            logger.info("record: $record")
            currentOffset[TopicPartition(record.topic(), record.partition())] =
                OffsetAndMetadata(record.offset() + 1, null)
            consumer.commitSync(currentOffset)
        }
    }
}

class RebalanceListener(
    private val consumer: KafkaConsumer<String, String>,
    private val currentOffset: Map<TopicPartition, OffsetAndMetadata>
) : ConsumerRebalanceListener {
    override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>) {
        logger.warn("Partitions are assigned")
    }

    override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>) {
        logger.warn("Partitions are revoked")
        consumer.commitSync(currentOffset)
    }
}
