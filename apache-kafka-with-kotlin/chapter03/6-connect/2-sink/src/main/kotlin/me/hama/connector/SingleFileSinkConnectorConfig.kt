package me.hama.connector

import org.apache.kafka.common.config.AbstractConfig
import org.apache.kafka.common.config.ConfigDef

class SingleFileSinkConnectorConfig(props: Map<String, String>) : AbstractConfig(CONFIG, props) {

    companion object {
        const val DIR_FILE_NAME = "file"
        private const val DIR_FILE_NAME_DEFAULT_VALUE = "/tmp/kafka.txt"
        private const val DIR_FILE_NAME_DOC = "/tmp/target.txt"

        val CONFIG: ConfigDef = ConfigDef().define(
            DIR_FILE_NAME,
            ConfigDef.Type.STRING,
            DIR_FILE_NAME_DEFAULT_VALUE,
            ConfigDef.Importance.HIGH,
            DIR_FILE_NAME_DOC
        )
    }
}
