package me.hama.file.flat

import me.hama.file.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@EnableBatchProcessing
@SpringBootApplication
class FlatTextFileJob(
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilderFactory: JobBuilderFactory
) {
    @Bean
    @StepScope
    fun customerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerFileReader")
            .resource(inputFile!!)
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zip")
            .targetType(Customer::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun formattedCustomerItemWriter(
        @Value("#{jobParameters['formattedOutputFile']}") outputFile: Resource?
    ): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerItemWriter")
            .resource(outputFile!!)
            .formatted()
            .format("%s %s lives at %s %s in %s, %s.")
            .names("firstName", "lastName", "address", "city", "state", "zip")
            .build()
    }

    @Bean
    @StepScope
    fun delimitedCustomerItemWriter(
        @Value("#{jobParameters['delimitedOutputFile']}") outputFile: Resource?
    ): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerItemWriter")
            .resource(outputFile!!)
            .delimited()
            .delimiter(";")
            .names("zip", "state", "city", "address", "lastName", "firstName")
            .build()
    }

    @Bean
    fun formatStep(): Step {
        return stepBuilderFactory["formatStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader(null))
            .writer(formattedCustomerItemWriter(null))
            .build()
    }

    @Bean
    fun delimitStep(): Step {
        return stepBuilderFactory["delimitStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader(null))
            .writer(delimitedCustomerItemWriter(null))
            .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory["job"]
            .start(formatStep())
            .next(delimitStep())
            .incrementer(RunIdIncrementer())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<FlatTextFileJob>(*args)
}
