package me.hama.custom

import me.hama.Customer
import org.springframework.batch.item.ItemProcessor

class EvenFilteringItemProcessor : ItemProcessor<Customer, Customer> {
    override fun process(item: Customer): Customer? {
        return if (item.zip.toInt() % 2 == 0) null else item
    }
}
