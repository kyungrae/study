package me.hama.connector

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

private val logger = LoggerFactory.getLogger(SingleFileSourceTask::class.java)
private const val FILENAME_FIELD = "filename"
private const val POSITION_FIELD = "position"

class SingleFileSourceTask : SourceTask() {
    private lateinit var fileNamePartition: Map<String, String>
    private lateinit var offset: Map<String, Any>
    private lateinit var topic: String
    private lateinit var file: String
    private var position: Long = -1

    override fun version(): String = "1.0"

    override fun start(props: MutableMap<String, String>) {
        try {
            val config = SingleFileSourceConnectorConfig(props)
            topic = config.getString(SingleFileSourceConnectorConfig.TOPIC_NAME)
            file = config.getString(SingleFileSourceConnectorConfig.DIR_FILE_NAME)
            fileNamePartition = mapOf(FILENAME_FIELD to file)
            offset = context.offsetStorageReader().offset(fileNamePartition)

            if (offset != null) {
                val lastReadFileOffset = offset[POSITION_FIELD]
                if (lastReadFileOffset != null) {
                    position = lastReadFileOffset as Long
                }
            } else {
                position = 0
            }
        } catch (e: Exception) {
            throw ConnectException(e.message, e)
        }
    }

    override fun stop() {}

    override fun poll(): MutableList<SourceRecord> {
        val results = mutableListOf<SourceRecord>()
        try {
            Thread.sleep(1000)

            val lines = getLines(position)

            if (lines.isNotEmpty()) {
                lines.forEach { line ->
                    val sourceOffset = mapOf(POSITION_FIELD to ++position)
                    val sourceRecord = SourceRecord(fileNamePartition, sourceOffset, topic, Schema.STRING_SCHEMA, line)
                    results += sourceRecord
                }
            }
            return results
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw ConnectException(e.message, e)
        }
    }

    private fun getLines(readLine: Long): List<String> {
        val reader = Files.newBufferedReader(Paths.get(file))
        return reader.lines().skip(readLine).collect(Collectors.toList())
    }
}
