package me.hama.file.multiformat

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource

@EnableBatchProcessing
@SpringBootApplication
class MultiFormatJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerTransactionItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): FlatFileItemReader<Any> {
        return FlatFileItemReaderBuilder<Any>()
            .name("customerTransactionItemReader")
            .lineMapper(lineTokenizer())
            .resource(inputFile!!)
            .build()
    }

    @Bean
    fun lineTokenizer(): PatternMatchingCompositeLineMapper<Any> {
        val lineTokenizers = mapOf(
            "CUST*" to customerLineTokenizer(),
            "TRANS*" to transactionLineTokenizer()
        )
        val customerFieldSetMapper = BeanWrapperFieldSetMapper<Any>();
		customerFieldSetMapper.setTargetType(Customer::class.java)
        val fieldSetMappers = mapOf(
            "CUST*" to customerFieldSetMapper,
            "TRANS*" to TransactionFieldSetMapper()
        )

        val lineMappers = PatternMatchingCompositeLineMapper<Any>()
        lineMappers.setTokenizers(lineTokenizers)
        lineMappers.setFieldSetMappers(fieldSetMappers)
        return lineMappers
    }

    @Bean
    fun customerLineTokenizer(): LineTokenizer {
        val lineTokenizer = DelimitedLineTokenizer()
        lineTokenizer.setNames("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode")
        lineTokenizer.setIncludedFields(1, 2, 3, 4, 5, 6, 7)
        return lineTokenizer
    }

    @Bean
    fun transactionLineTokenizer(): LineTokenizer {
        val lineTokenizer = DelimitedLineTokenizer()
        lineTokenizer.setNames("prefix", "accountNumber", "transactionDate", "amount")
        return lineTokenizer
    }

    @Bean
    fun itemWriter(): ItemWriter<Any> = ItemWriter<Any> { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Any, Any>(10)
            .reader(customerTransactionItemReader(null))
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
    runApplication<MultiFormatJob>(*args)
}
