package me.hama.connector

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceConnector

class SingleFileSourceConnector : SourceConnector() {
    private var configProperties: Map<String, String> = mutableMapOf()

    override fun version(): String = "1.0"

    override fun start(props: Map<String, String>) {
        this.configProperties = props
        try {
            SingleFileSourceConnectorConfig(props)
        } catch (e: ConfigException) {
            throw ConnectException(e.message, e)
        }
    }

    override fun taskClass(): Class<out Task> = SingleFileSourceTask::class.java

    override fun taskConfigs(maxTasks: Int): List<Map<String, String>> {
        var taskConfigs = mutableListOf<Map<String, String>>()
        var taskProps = mutableMapOf<String, String>()
        taskProps.putAll(configProperties)
        for (i in 0 until maxTasks) {
            taskConfigs.add(taskProps)
        }
        return taskConfigs
    }

    override fun stop() {}

    override fun config(): ConfigDef = SingleFileSourceConnectorConfig.CONFIG
}
