package me.hama.client.producer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val TOPIC_NAME = "test"
const val BOOTSTRAP_SERVERS = "localhost:9092"

val logger: Logger = LoggerFactory.getLogger("ProducerLogger")
