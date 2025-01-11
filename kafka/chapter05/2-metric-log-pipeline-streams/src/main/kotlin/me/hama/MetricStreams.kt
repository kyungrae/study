package me.hama

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.*

lateinit var streams: KafkaStreams

fun main() {
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })

    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "metric-streams-application"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val builder = StreamsBuilder()
    val metrics = builder.stream<String, String>("metric.all")
    val metricBranch = metrics.branch(
        { _, value -> getMetricName(value) == "cpu" },
        { _, value -> getMetricName(value) == "memory" }
    )

    metricBranch[0].to("metric.cpu")
    metricBranch[1].to("metric.memory")

    val filteredCpuMetric = metricBranch[0].filter { _, value -> getTotalCpuPercent(value) > 0.5 }
    filteredCpuMetric.mapValues { value -> getHostTimeStamp(value) }.to("metric.cpu.alter")

    streams = KafkaStreams(builder.build(), props)
    streams.start()
}
