package me.hama.client.consumer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val TOPIC_NAME = "test"
const val BOOTSTRAP_SERVERS = "localhost:9092"
const val GROUP_ID = "test-group"

val logger: Logger = LoggerFactory.getLogger("ConsumerLogger")
