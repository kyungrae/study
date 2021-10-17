package me.hama.connector

import org.apache.kafka.common.config.AbstractConfig
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigDef.Importance

class SingleFileSourceConnectorConfig(props: Map<String, String>) : AbstractConfig(CONFIG, props) {
    companion object {
        const val DIR_FILE_NAME = "file"
        private const val DIR_FILE_NAME_DEFAULT_VALUE = "/tmp/kafka.txt"
        private const val DIR_FILE_NAME_DOC = "/tmp/target.txt"
        const val TOPIC_NAME = "topic"
        private const val TOPIC_DEFAULT_VALUE = "test"
        private const val TOPIC_DOC = "connect_source"
        val CONFIG: ConfigDef = ConfigDef()
            .define(
                DIR_FILE_NAME,
                ConfigDef.Type.STRING,
                DIR_FILE_NAME_DEFAULT_VALUE,
                Importance.HIGH,
                DIR_FILE_NAME_DOC
            )
            .define(
                TOPIC_NAME,
                ConfigDef.Type.STRING,
                TOPIC_DEFAULT_VALUE,
                Importance.HIGH,
                TOPIC_DOC
            )
    }
}
