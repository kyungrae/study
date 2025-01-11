package me.hama.client.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.Cluster
import org.apache.kafka.common.InvalidRecordException
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.utils.Utils
import java.util.*

class CustomPartitioner : Partitioner {
    override fun configure(configs: MutableMap<String, *>) {}

    override fun close() {}

    override fun partition(
        topic: String?,
        key: Any?,
        keyBytes: ByteArray?,
        value: Any?,
        valueBytes: ByteArray?,
        cluster: Cluster
    ): Int {
        if (keyBytes == null)
            throw InvalidRecordException("Need message key")
        if (key == "Incheon") return 0

        val partitions = cluster.partitionsForTopic(topic)
        val numPartitions = partitions.size
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions
    }
}

fun main() {
    val configs = Properties()
    configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    configs[ProducerConfig.PARTITIONER_CLASS_CONFIG] = CustomPartitioner::class.java

    val producer = KafkaProducer<String, String>(configs)

    val record = ProducerRecord(TOPIC_NAME, "Incheon", "29")
    producer.send(record)
    producer.flush()
    producer.close()
}
