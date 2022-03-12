package me.hama.validation

import me.hama.UniqueLastNameValidator
import me.hama.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.validator.ValidatingItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@EnableBatchProcessing
@SpringBootApplication
class ValidationJob(
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
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zip")
            .targetType(Customer::class.java)
            .resource(inputFile!!)
            .build()
    }

//    @Bean
//    fun customerValidatingItemProcessor(): ValidatingItemProcessor<Customer> =
//        BeanValidatingItemProcessor()

    @Bean
    fun validator(): UniqueLastNameValidator {
        val uniqueLastNameValidator = UniqueLastNameValidator()
        uniqueLastNameValidator.setName("validator")
        return uniqueLastNameValidator
    }

    @Bean
    fun customerValidatingItemProcessor(): ValidatingItemProcessor<Customer> =
        ValidatingItemProcessor(validator())

    @Bean
    fun itemWriter() = ItemWriter<Customer> { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(5)
            .reader(customerItemReader(null))
            .processor(customerValidatingItemProcessor())
            .writer(itemWriter())
            .stream(validator())
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
    runApplication<ValidationJob>(*args)
}
