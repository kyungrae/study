package me.hama.database.existingservice

import me.hama.database.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.adapter.ItemReaderAdapter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
class ExistingServiceJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val customerService: CustomerService
) {
    @Bean
    fun customerItemReader(customerService: CustomerService): ItemReaderAdapter<Customer> {
        val adapter = ItemReaderAdapter<Customer>()
        adapter.setTargetObject(customerService)
        adapter.setTargetMethod("getCustomer")
        return adapter
    }

    @Bean
    fun itemWriter() = ItemWriter<Customer> { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader(customerService))
            .writer(itemWriter())
            .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory["job"]
            .start(copyFileStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<ExistingServiceJob>(*args)
}
