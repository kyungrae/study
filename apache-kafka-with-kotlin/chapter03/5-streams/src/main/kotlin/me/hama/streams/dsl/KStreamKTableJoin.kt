package me.hama.streams.dsl

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.*

fun main() {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = APPLICATION_NAME
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val builder = StreamsBuilder()
    val addressTable = builder.table<String, String>(ADDRESS_TABLE)
    val orderStream = builder.stream<String, String>(ORDER_STREAM)

    orderStream
        .join(addressTable) { order, address -> "$order send to $address" }
        .to(ORDER_JOIN_STREAM)

    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
