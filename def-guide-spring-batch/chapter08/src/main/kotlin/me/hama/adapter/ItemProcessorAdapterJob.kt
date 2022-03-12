package me.hama.adapter

import me.hama.UpperCaseNameService
import me.hama.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.adapter.ItemProcessorAdapter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@SpringBootApplication
@EnableBatchProcessing
class ItemProcessorAdapterJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
) {
    @StepScope
    @Bean
    fun customerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zip")
            .targetType(Customer::class.java)
            .resource(inputFile!!)
            .build()
    }

    @Bean
    fun upperCaseNameService(): UpperCaseNameService {
        return UpperCaseNameService()
    }

    @Bean
    fun itemProcessor(service: UpperCaseNameService?): ItemProcessorAdapter<Customer, Customer> {
        val itemProcessorAdapter = ItemProcessorAdapter<Customer, Customer>()
        itemProcessorAdapter.setTargetObject(service!!)
        itemProcessorAdapter.setTargetMethod("upperCase")
        return itemProcessorAdapter
    }

    @Bean
    fun itemWriter() = ItemWriter<Customer>(::println)

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(5)
            .reader(customerItemReader(null))
            .writer(itemWriter())
            .processor(itemProcessor(null))
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
    runApplication<ItemProcessorAdapterJob>(*args)
}
