package me.hama.file.fix

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.transform.Range
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@EnableBatchProcessing
@SpringBootApplication
class FixedWidthJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    @StepScope
    fun customerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .resource(inputFile!!)
            .fixedLength()
            .columns(
                Range(1, 11),
                Range(12, 12),
                Range(13, 22),
                Range(23, 26),
                Range(27, 46),
                Range(47, 62),
                Range(63, 64),
                Range(65, 69)
            )
            .names("firstName", "middleInitial", "lastName", "addressNumber", "street", "city", "state", "zipCode")
            .targetType(Customer::class.java)
            .build()
    }

    @Bean
    fun itemWriter(): ItemWriter<Customer> =
        ItemWriter { items -> items.forEach(::println) }

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
    runApplication<FixedWidthJob>(*args)
}
