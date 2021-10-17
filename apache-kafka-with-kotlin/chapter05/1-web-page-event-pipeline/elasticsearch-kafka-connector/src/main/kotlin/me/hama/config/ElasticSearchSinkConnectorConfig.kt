package me.hama.config

import org.apache.kafka.common.config.AbstractConfig
import org.apache.kafka.common.config.ConfigDef

class ElasticSearchSinkConnectorConfig(props: Map<String, String>) : AbstractConfig(CONFIG, props) {

    companion object {
        const val ES_CLUSTER_HOST = "es.host"
        private const val ES_CLUSTER_HOST_DEFAULT_VALUE = "localhost"
        private const val ES_CLUSTER_HOST_DOC = "localhost"

        const val ES_CLUSTER_PORT = "es.port"
        private const val ES_CLUSTER_PORT_DEFAULT_VALUE = "9200"
        private const val ES_CLUSTER_PORT_DOC = "9200"

        const val ES_INDEX = "es.index"
        private const val ES_INDEX_DEFAULT_VALUE = "kafka-connector-index"
        private const val ES_INDEX_doc = "kafka-connector-index"

        val CONFIG: ConfigDef = ConfigDef()
            .define(ES_CLUSTER_HOST, ConfigDef.Type.STRING, ES_CLUSTER_HOST_DEFAULT_VALUE, ConfigDef.Importance.HIGH, ES_CLUSTER_HOST_DOC)
            .define(ES_CLUSTER_PORT, ConfigDef.Type.STRING, ES_CLUSTER_PORT_DEFAULT_VALUE, ConfigDef.Importance.HIGH, ES_CLUSTER_PORT_DOC)
            .define(ES_INDEX, ConfigDef.Type.STRING, ES_INDEX_DEFAULT_VALUE, ConfigDef.Importance.HIGH, ES_INDEX_doc)
    }
}
