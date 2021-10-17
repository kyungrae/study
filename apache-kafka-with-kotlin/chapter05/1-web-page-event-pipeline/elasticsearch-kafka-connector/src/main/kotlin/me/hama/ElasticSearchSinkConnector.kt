package me.hama

import me.hama.config.ElasticSearchSinkConnectorConfig
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.SinkConnector
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(ElasticSearchSinkConnector::class.java)

class ElasticSearchSinkConnector: SinkConnector() {

    private lateinit var configProperties: Map<String, String>

    override fun version() = "1.0"

    override fun start(props: MutableMap<String, String>) {
        this.configProperties = props
        try {
            ElasticSearchSinkConnectorConfig(props)
        } catch (e: ConfigException) {
            throw ConnectException(e.message, e)
        }
    }

    override fun taskClass(): Class<out Task> = ElasticSearchSinkTask::class.java

    override fun taskConfigs(maxTasks: Int): MutableList<MutableMap<String, String>> {
        val taskProps = hashMapOf<String, String>()
        taskProps.putAll(configProperties)
        return MutableList(maxTasks) { taskProps }
    }

    override fun stop() {
        logger.info("Stop elasticsearch connector")
    }

    override fun config(): ConfigDef = ElasticSearchSinkConnectorConfig.CONFIG
}
