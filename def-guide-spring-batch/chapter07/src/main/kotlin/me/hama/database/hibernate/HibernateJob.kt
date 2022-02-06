package me.hama.database.hibernate

import org.hibernate.SessionFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.HibernateCursorItemReader
import org.springframework.batch.item.database.HibernatePagingItemReader
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.persistence.EntityManagerFactory

@SpringBootApplication
@EnableBatchProcessing
class HibernateJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
//    @Bean
//    @StepScope
//    fun customerItemReader(
//        entityManagerFactory: EntityManagerFactory?, @Value("#{jobParameters['city']}") city: String?
//    ): HibernateCursorItemReader<Customer> {
//        return HibernateCursorItemReaderBuilder<Customer>()
//            .name("customerItemReader")
//            .sessionFactory(entityManagerFactory!!.unwrap(SessionFactory::class.java))
//            .queryString("from Customer where city like :city")
//            .parameterValues(mapOf("city" to city))
//            .build()
//    }

    @Bean
    @StepScope
    fun customerItemReader(
        entityManagerFactory: EntityManagerFactory?, @Value("#{jobParameters['city']}") city: String?
    ): HibernatePagingItemReader<Customer> {
        return HibernatePagingItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .sessionFactory(entityManagerFactory!!.unwrap(SessionFactory::class.java))
            .queryString("from Customer where city like :city")
            .parameterValues(mapOf("city" to city))
            .pageSize(10)
            .build()
    }

    @Bean
    fun itemWriter() = ItemWriter<Customer> { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader(null, null))
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
    runApplication<HibernateJob>(*args)
}
