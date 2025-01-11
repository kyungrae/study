package me.hama

import com.google.gson.Gson
import me.hama.config.ElasticSearchSinkConnectorConfig
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.SinkRecord
import org.apache.kafka.connect.sink.SinkTask
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.Exception

private val logger: Logger = LoggerFactory.getLogger(ElasticSearchSinkConnector::class.java)

class ElasticSearchSinkTask : SinkTask() {

    private lateinit var config: ElasticSearchSinkConnectorConfig
    private lateinit var esClient: RestHighLevelClient

    override fun version() = "1.0"

    override fun start(props: MutableMap<String, String>) {
        try {
            config = ElasticSearchSinkConnectorConfig(props)
        } catch (e: ConfigException) {
            throw ConnectException(e.message, e)
        }

        val host = config.getString(ElasticSearchSinkConnectorConfig.ES_CLUSTER_HOST)
        val port = config.getString(ElasticSearchSinkConnectorConfig.ES_CLUSTER_PORT)
        esClient = RestHighLevelClient(RestClient.builder(HttpHost(host, port.toInt())))
    }

    override fun put(records: MutableCollection<SinkRecord>) {
        if (records.isNotEmpty()) {
            val bulkRequest = BulkRequest()
            for (record in records) {
                val gson = Gson()
                val map = gson.fromJson(record.value().toString(), Map::class.java)
                bulkRequest.add(
                    IndexRequest(config.getString(ElasticSearchSinkConnectorConfig.ES_INDEX)).source(
                        map,
                        XContentType.JSON
                    )
                )
                logger.info("record: ${record.value()}")
            }

            esClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, object : ActionListener<BulkResponse> {
                override fun onResponse(response: BulkResponse) {
                    if (response.hasFailures())
                        logger.error(response.buildFailureMessage())
                    else
                        logger.info("bulk save success")
                }

                override fun onFailure(e: Exception) {
                    logger.error(e.message, e)
                }
            })
        }
    }

    override fun flush(currentOffsets: MutableMap<TopicPartition, OffsetAndMetadata>) {
        logger.info("flush")
    }

    override fun stop() {
        try {
            esClient.close()
        } catch (e: IOException) {
            logger.error(e.message, e)
        }
    }
}
