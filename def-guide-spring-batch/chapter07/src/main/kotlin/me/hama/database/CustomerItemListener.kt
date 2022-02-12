package me.hama.database

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.item.file.FlatFileParseException
import java.lang.Exception

class CustomerItemListener : ItemReadListener<Customer> {
    override fun beforeRead() {}

    override fun afterRead(item: Customer) {}

    override fun onReadError(ex: Exception) {
        if (ex is FlatFileParseException) {

            val message = buildString {
                append("An error occured while processing the ${ex.lineNumber} line of the file. Below was the faulty input.\n")
                append("${ex.input}\n")
            }
            logger.error(message, ex)
        } else {
            logger.error("An error has occrred", ex)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CustomerItemListener::class.java)
    }
}
