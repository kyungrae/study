package me.hama.connector

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.SinkConnector

class SingleFileSinkConnector : SinkConnector() {

    private var config: Map<String, String> = mapOf()

    override fun version(): String = "1.0"

    override fun start(props: MutableMap<String, String>) {
        config = props
        try {
            SingleFileSinkConnectorConfig(props)
        } catch (e: ConfigException) {
            throw ConnectException(e.message, e)
        }
    }

    override fun taskClass(): Class<out Task> = SingleFileSinkTask::class.java

    override fun taskConfigs(maxTasks: Int): List<Map<String, String>> {
        val taskConfigs = mutableListOf<Map<String, String>>()
        val taskProps = mutableMapOf<String, String>()
        taskProps.putAll(config)
        for (i in 0 until maxTasks) {
            taskConfigs.add(taskProps)
        }
        return taskConfigs
    }

    override fun stop() {}

    override fun config(): ConfigDef = SingleFileSinkConnectorConfig.CONFIG
}
