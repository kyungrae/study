package me.hama.file.multiresource

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream
import org.springframework.core.io.Resource

class CustomerFileReader(
    private val delegate: ResourceAwareItemReaderItemStream<Any>
) : ResourceAwareItemReaderItemStream<Customer> {
    private var curItem: Any? = null

    override fun open(executionContext: ExecutionContext) {
        delegate.open(executionContext)
    }

    override fun update(executionContext: ExecutionContext) {
        delegate.update(executionContext)
    }

    override fun close() {
        delegate.close()
    }

    override fun read(): Customer? {
        if (curItem == null)
            curItem = delegate.read()

        val item = curItem as Customer?
        curItem = null

        if (item != null) {
            item.transactions = mutableListOf()

            while (peek() is Transaction) {
                item.transactions.add(curItem as Transaction)
                curItem = null
            }
        }
        return item
    }

    private fun peek(): Any? {
        if (curItem == null) {
            curItem = delegate.read()
        }
        return curItem
    }

    override fun setResource(resource: Resource) {
        delegate.setResource(resource)
    }
}
