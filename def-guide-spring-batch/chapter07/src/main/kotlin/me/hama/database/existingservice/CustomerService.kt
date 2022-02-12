package me.hama.database.existingservice

import me.hama.database.Customer
import org.springframework.stereotype.Component
import kotlin.arrayOf
import kotlin.random.Random

@Component
class CustomerService {
    private var customers: List<Customer> = List(100) { buildCustomer() }
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

    fun getCustomer(): Customer? {
        var cust: Customer? = null
        if (curIndex < customers.size) {
            cust = customers[curIndex]
            curIndex++
        }
        return cust
    }

    companion object {
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
