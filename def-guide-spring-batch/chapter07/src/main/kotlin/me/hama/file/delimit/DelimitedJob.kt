package me.hama.file.delimit

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@EnableBatchProcessing
@SpringBootApplication
class DelimitedJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerItemReader(@Value("#{jobParameters['customFile']") inputFile: Resource?): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .delimited()
            .names("firstName", "middleInitial", "lastName", "addressNumber", "street", "city", "state", "zipCode")
            .fieldSetMapper(CustomerFieldSetMapper())
            .resource(inputFile!!)
            .build()
    }

    @Bean
    @StepScope
    fun customerItemReaderWithCustomTokenizer(
        @Value("#{jobParameters['customFile']") inputFile: Resource?
    ): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .lineTokenizer(CustomerFileLineTokenizer())
            .targetType(Customer::class.java)
            .resource(inputFile!!)
            .build()
    }

    @Bean
    fun itemWriter(): ItemWriter<Customer> = ItemWriter<Customer> { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader(null))
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
    runApplication<DelimitedJob>(*args)
}
