package me.hama.database.custom

import me.hama.database.Customer
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStreamSupport
import kotlin.random.Random

class CustomerItemReader : ItemStreamSupport(), ItemReader<Customer> {
    private val customers: List<Customer> = List(100) { buildCustomer() }
    private var curIndex = 0

    private fun buildCustomer(): Customer {
        return Customer(
            id = Random.nextLong(),
            firstName = firstNames[Random.nextInt(firstNames.size - 1)],
            middleInitial = middleInitial[Random.nextInt(middleInitial.length - 1)].toString(),
            lastName = lastNames[Random.nextInt(lastNames.size - 1)],
            address = Random.nextInt(9999).toString() + " " + streets[Random.nextInt(states.size - 1)],
            city = cities[Random.nextInt(cities.size - 1)],
            state = states[Random.nextInt(states.size - 1)],
            zipCode = Random.nextInt(99999).toString()
        )
    }

    override fun read(): Customer? {
        var cust: Customer? = null

        if (curIndex == 50) {
            throw RuntimeException("This will end your execution")
        }

        if (curIndex < customers.size) {
            cust = customers[curIndex]
            curIndex++
        }
        return cust
    }

    override fun open(executionContext: ExecutionContext) {
        curIndex = if (executionContext.containsKey(getExecutionContextKey(INDEX_KEY))) {
            val index = executionContext.getInt((getExecutionContextKey(INDEX_KEY)))
            if (index == 50) {
                51
            } else
                index
        } else {
            0
        }
    }

    override fun update(executionContext: ExecutionContext) {
        executionContext.putInt(getExecutionContextKey(INDEX_KEY), curIndex)
    }

    override fun close() {
    }

    companion object {
        private const val INDEX_KEY = "current.index.customers"

        private val firstNames = arrayOf(
            "Michael", "Warren", "Ann", "Terrence",
            "Erica", "Laura", "Steve", "Larry"
        )
        private const val middleInitial = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val lastNames = arrayOf(
            "Gates", "Darrow", "Donnelly", "Jobs",
            "Buffett", "Ellison", "Obama"
        )
        private val streets = arrayOf(
            "4th Street", "Wall Street", "Fifth Avenue",
            "Mt. Lee Drive", "Jeopardy Lane",
            "Infinite Loop Drive", "Farnam Street",
            "Isabella Ave", "S. Greenwood Ave"
        )
        private val cities = arrayOf(
            "Chicago", "New York", "Hollywood", "Aurora",
            "Omaha", "Atherton"
        )
        private val states = arrayOf("IL", "NY", "CA", "NE")
    }
}
