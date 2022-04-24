package me.hama.database.jdbc

import me.hama.database.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import javax.sql.DataSource

@SpringBootApplication
@EnableBatchProcessing
class JdbcJob(
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilderFactory: JobBuilderFactory
) {
    @Bean
    @StepScope
    fun customerFileReader(
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
    fun jdbcBatchItemWriter(dataSource: DataSource?): JdbcBatchItemWriter<Customer> {
        return JdbcBatchItemWriterBuilder<Customer>()
            .dataSource(dataSource!!)
            .sql("INSERT INTO CUSTOMER(first_name,middle_initial,last_name,address,city,state,zip) values(?,?,?,?,?,?,?)")
            .itemPreparedStatementSetter(CustomerItemPreparedStatementSetter())
            .build()
    }

    @Bean
    fun jdbcFormatStep(): Step {
        return stepBuilderFactory["jdbcFormatStep"]
            .chunk<Customer, Customer>(3)
            .reader(customerFileReader(null))
            .writer(jdbcBatchItemWriter(null))
            .build()
    }

    @Bean
    fun jdbcFormatJob(): Job {
        return jobBuilderFactory["jdbcFormatJob"]
            .start(jdbcFormatStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<JdbcJob>(*args)
}
