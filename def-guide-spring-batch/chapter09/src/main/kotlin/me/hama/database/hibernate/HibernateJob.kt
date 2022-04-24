package me.hama.database.hibernate

import org.hibernate.SessionFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.database.HibernateItemWriter
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder
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
class HibernateJob(
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
    fun hibernateItemWriter(entityManagerFactory: EntityManagerFactory?): HibernateItemWriter<Customer> {
        return HibernateItemWriterBuilder<Customer>()
            .sessionFactory(entityManagerFactory!!.unwrap(SessionFactory::class.java))
            .build()
    }

    @Bean
    fun hibernateFormatStep(): Step {
        return stepBuilderFactory["hibernateStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerFileReader(null))
            .writer(hibernateItemWriter(null))
            .build()
    }

    @Bean
    fun hibernateFormatJob(): Job {
        return jobBuilderFactory["hibernateFormatJob"]
            .start(hibernateFormatStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<HibernateJob>(*args)
}
