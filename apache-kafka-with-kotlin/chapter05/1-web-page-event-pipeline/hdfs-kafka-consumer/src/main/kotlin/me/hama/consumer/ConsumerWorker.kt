package me.hama.consumer

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(ConsumerWorker::class.java)

class ConsumerWorker(
    private val prop: Properties,
    private val topic: String,
    number: Int
) : Runnable {

    private lateinit var consumer: KafkaConsumer<String, String>
    private val threadName = "consumer-thread-$number"

    fun stopAndWakeup() {
        logger.info("stopAndWakeup")
        consumer.wakeup()
        saveRemainBufferToHdfsFile()
    }

    override fun run() {
        Thread.currentThread().name = threadName
        consumer = KafkaConsumer(prop)
        consumer.subscribe(listOf(topic))

        try {
            while (true) {
                val records = consumer.poll(Duration.ofSeconds(1))

                for (record in records) {
                    addHdfsFileBuffer(record)
                }
                saveBufferToHdfsFile(consumer.assignment())
            }
        } catch (e: WakeupException) {
            logger.warn("Wakeup consumer")
        } catch (e: Exception) {
            logger.error(e.message, e)
        } finally {
            consumer.close()
        }
    }

    private fun addHdfsFileBuffer(record: ConsumerRecord<String, String>) {
        val buffer = bufferString[record.partition()] ?: mutableListOf()
        buffer += record.value()
        bufferString[record.partition()] = buffer
        if (buffer.size == 1)
            currentFileOffset[record.partition()] = record.offset()
    }

    private fun saveBufferToHdfsFile(partitions: Set<TopicPartition>) {
        partitions.forEach { checkFlushCount(it.partition()) }
    }

    private fun checkFlushCount(partitionNo: Int) {
        val records = bufferString[partitionNo]
        if (records != null) {
            if (records.size >= FLUSH_RECORD_COUNT) {
                save(partitionNo)
            }
        }
    }

    private fun save(partitionNo: Int) {
        if (bufferString[partitionNo]!!.size > 0) try {
            val fileName = "/data/color-$partitionNo-${currentFileOffset[partitionNo]}.log"
            val configuration = Configuration()
            configuration["fs.defaultFS"] = "hdfs://localhost:9000"
            val hdfsFileSystem = FileSystem.get(configuration)
            val fileOutputStream = hdfsFileSystem.create(Path(fileName))
            fileOutputStream.writeBytes(StringUtils.join(bufferString[partitionNo], "\n"))
            fileOutputStream.close()
            bufferString[partitionNo] = mutableListOf()
        } catch (e: java.lang.Exception) {
            logger.error(e.message, e)
        }
    }

    private fun saveRemainBufferToHdfsFile() {
        bufferString.forEach { (partitionNo, v) -> save(partitionNo) }
    }

    companion object {
        private const val FLUSH_RECORD_COUNT = 10;
        private val bufferString: MutableMap<Int, MutableList<String>> = mutableMapOf()
        private val currentFileOffset: MutableMap<Int, Long> = mutableMapOf()
    }
}
