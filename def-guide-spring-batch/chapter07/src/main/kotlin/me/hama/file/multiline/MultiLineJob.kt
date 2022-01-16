package me.hama.file.multiline

import me.hama.file.multiformat.Customer
import me.hama.file.multiformat.Transaction
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.FieldSetMapper
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
class MultiLineJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): FlatFileItemReader<Any> {
        return FlatFileItemReaderBuilder<Any>()
            .name("customerItemReader")
            .lineMapper(lineTokenizer())
            .resource(inputFile!!)
            .build()
    }

    @Bean
    fun customerFileReader(): CustomerFileReader {
        return CustomerFileReader(customerItemReader(null))
    }

    @Bean
    fun lineTokenizer(): PatternMatchingCompositeLineMapper<Any> {
        val lineTokenizers = mapOf(
            "CUST*" to customerLineTokenizer(),
            "TRANS*" to transactionLineTokenizer()
        )

        val fieldSetMappers = mapOf<String, FieldSetMapper<Any>>(
            "CUST*" to FieldSetMapper {
                Customer(
                    firstName = it.readString("firstName"),
                    middleInitial = it.readString("middleInitial"),
                    lastName = it.readString("lastName"),
                    address = it.readString("address"),
                    city = it.readString("city"),
                    state = it.readString("state"),
                    zipCode = it.readString("zipCode")
                )
            },
            "TRANS*" to FieldSetMapper {
                Transaction(
                    it.readString("accountNumber"),
                    it.readDate("transactionDate", "yyyy-MM-dd HH:mm:ss"),
                    it.readDouble("amount")
                )
            }
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
            .chunk<Customer, Customer>(10) // I don't want change this to chunk<Customer, Customer>(10)
            .reader(customerFileReader()) // This line has problem about covariant
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
    runApplication<MultiLineJob>(*args)
}
