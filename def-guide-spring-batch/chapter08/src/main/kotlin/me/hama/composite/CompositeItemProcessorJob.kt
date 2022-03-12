package me.hama.composite

import me.hama.Customer
import me.hama.UniqueLastNameValidator
import me.hama.UpperCaseNameService
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
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.batch.item.support.ScriptItemProcessor
import org.springframework.batch.item.validator.ValidatingItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@SpringBootApplication
@EnableBatchProcessing
class CompositeItemProcessorJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @StepScope
    @Bean
    fun customerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerReader")
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zip")
            .targetType(Customer::class.java)
            .resource(inputFile!!)
            .build()
    }

    @Bean
    fun itemWriter() = ItemWriter<Customer> { println(it) }

    @Bean
    fun validator(): UniqueLastNameValidator {
        val validator = UniqueLastNameValidator()
        validator.setName("validator")
        return validator
    }

    @Bean
    fun customerValidatingItemProcessor(): ValidatingItemProcessor<Customer> {
        val itemProcessor = ValidatingItemProcessor(validator())
        itemProcessor.setFilter(true)
        return itemProcessor
    }

    @Bean
    fun upperCaseNameService(): UpperCaseNameService {
        return UpperCaseNameService()
    }

    @Bean
    fun upperCaseItemProcessor(service: UpperCaseNameService?): ItemProcessorAdapter<Customer, Customer> {
        val adapter = ItemProcessorAdapter<Customer, Customer>()
        adapter.setTargetObject(service!!)
        adapter.setTargetMethod("upperCase")
        return adapter
    }

    @Bean
    @StepScope
    fun lowerCaseItemProcessor(
        @Value("#{jobParameters['script']}") script: Resource?
    ): ScriptItemProcessor<Customer, Customer> {
        val itemProcessor = ScriptItemProcessor<Customer, Customer>()
        itemProcessor.setScript(script!!)
        return itemProcessor
    }

    @Bean
    fun itemProcessor(): CompositeItemProcessor<Customer, Customer> {
        val itemProcessor = CompositeItemProcessor<Customer, Customer>()
        itemProcessor.setDelegates(
            listOf(customerValidatingItemProcessor(), upperCaseItemProcessor(null), lowerCaseItemProcessor(null))
        )
        return itemProcessor
    }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(5)
            .reader(customerItemReader(null))
            .writer(itemWriter())
            .processor(itemProcessor())
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
    runApplication<CompositeItemProcessorJob>(*args)
}
