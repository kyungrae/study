package me.hama.file.multiline

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamReader

class CustomerFileReader(
    private val delegate: ItemStreamReader<Any>
) : ItemStreamReader<Customer> {
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

        var item = curItem as Customer?
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
}
