package me.hama.database.custom

import me.hama.database.Customer
import me.hama.database.CustomerItemListener
import me.hama.database.EmptyInputStepFailer
import me.hama.database.FileVerificationSkipper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
class CustomInputJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun customerItemReader(): CustomerItemReader {
        val customerItemReader = CustomerItemReader()
        customerItemReader.setName("customerItemReader")
        return customerItemReader
    }

    @Bean
    fun itemWriter() = ItemWriter<Customer> { it.forEach(::println) }

    @Bean
    fun emptyFileFailer() = EmptyInputStepFailer()

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader())
            .writer(itemWriter())
            .faultTolerant()
            .skipPolicy(FileVerificationSkipper())
            .listener(CustomerItemListener())
            .listener(emptyFileFailer())
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
    runApplication<CustomInputJob>(*args)
}
