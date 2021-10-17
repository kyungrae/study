package me.hama.client.admin

import org.apache.kafka.clients.admin.*
import org.apache.kafka.common.config.ConfigResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

val logger: Logger = LoggerFactory.getLogger("AdminLogger")
const val BOOTSTRAP_SERVER = "localhost:9092"

fun main() {
    val configs = Properties()
    configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVER
    val adminClient = AdminClient.create(configs)
    logger.info("== Get broker information")
    for (node in adminClient.describeCluster().nodes().get()) {
        logger.info("node : $node")
        val cr = ConfigResource(ConfigResource.Type.BROKER, node.idString())
        val describeConfigs = adminClient.describeConfigs(setOf(cr))
        describeConfigs.all().get().forEach { (broker, config) ->
            config.entries().forEach { configEntry -> logger.info("${configEntry.name()} = ${configEntry.value()}") }
        }
    }

    logger.info("== Get default num.partitions")
    for (node in adminClient.describeCluster().nodes().get()) {
        val cr = ConfigResource(ConfigResource.Type.BROKER, node.idString())
        val describeConfigs: DescribeConfigsResult = adminClient.describeConfigs(setOf(cr))
        val config = describeConfigs.all().get()[cr]
        val optionalConfigEntry = config!!.entries().stream().filter { v ->
            v.name().equals("num.partitions")
        }.findFirst()
        val numPartitionConfig = optionalConfigEntry.orElseThrow(::Exception)
        logger.info("{}", numPartitionConfig.value())
    }

    logger.info("== Topic list")
    for (topicListing in adminClient.listTopics().listings().get()) {
        logger.info("{}", topicListing.toString())
    }

    logger.info("== test topic information")
    val topicInformation: Map<String, TopicDescription> = adminClient.describeTopics(listOf("test")).all().get()
    logger.info("{}", topicInformation)

    logger.info("== Consumer group list")
    val listConsumerGroups: ListConsumerGroupsResult = adminClient.listConsumerGroups()
    listConsumerGroups.all().get().forEach{ v: ConsumerGroupListing ->
        logger.info("$v")
    }

    adminClient.close()
}
