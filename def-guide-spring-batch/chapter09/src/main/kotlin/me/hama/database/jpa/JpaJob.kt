package me.hama.database.jpa

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import javax.persistence.EntityManagerFactory

@SpringBootApplication
@EnableBatchProcessing
class JpaJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerFileReader(@Value("#{jobParameters['customerFile']}") inputFile: Resource?): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerFileReader")
            .resource(inputFile!!)
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zip")
            .targetType(Customer::class.java)
            .build()
    }

    @Bean
    fun jpItemWriter(entityManagerFactory: EntityManagerFactory?): JpaItemWriter<Customer> {
        val jpaItemWriter = JpaItemWriter<Customer>()
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory!!)
        return jpaItemWriter
    }

    @Bean
    fun jpaFormatStep(): Step {
        return stepBuilderFactory["hibernateStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerFileReader(null))
            .writer(jpItemWriter(null))
            .build()
    }

    @Bean
    fun jpaFormatJob(): Job {
        return jobBuilderFactory["jpaFormatJob"]
            .start(jpaFormatStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<JpaJob>(*args)
}
