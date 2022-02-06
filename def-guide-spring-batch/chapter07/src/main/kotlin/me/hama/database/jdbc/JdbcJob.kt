package me.hama.database.jdbc

import me.hama.database.Customer
import me.hama.database.CustomerRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter
import javax.sql.DataSource

@SpringBootApplication
@EnableBatchProcessing
class JdbcJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val dataSource: DataSource
) {
    @Bean
    fun cursorCustomerItemReader(dataSource: DataSource): JdbcCursorItemReader<Customer> {
        return JdbcCursorItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .dataSource(dataSource)
            .sql("select * from CUSTOMER where city = ?")
            .rowMapper(CustomerRowMapper())
            .preparedStatementSetter(citySetter(null))
            .build()
    }

    @Bean
    @StepScope
    fun pagingCustomerItemReader(
        dataSource: DataSource, queryProvider: PagingQueryProvider?, @Value("#{jobParameters['city']}") city: String?
    ): JdbcPagingItemReader<Customer> {
        val parameterValues = mapOf("city" to city)
        return JdbcPagingItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider!!)
            .parameterValues(parameterValues)
            .pageSize(10)
            .rowMapper(CustomerRowMapper())
            .build()
    }

    @Bean
    fun pagingQueryProvider(dataSource: DataSource): SqlPagingQueryProviderFactoryBean {
        val factoryBean = SqlPagingQueryProviderFactoryBean()

        factoryBean.setSelectClause("select *")
        factoryBean.setFromClause("from CUSTOMER")
        factoryBean.setWhereClause("where city like :city")
        factoryBean.setSortKey("lastName")
        factoryBean.setDataSource(dataSource)

        return factoryBean
    }

    @Bean
    @StepScope
    fun citySetter(@Value("#{jobParameters['city']}") city: String?) =
        ArgumentPreparedStatementSetter(arrayOf(city))

    @Bean
    fun itemWriter(): ItemWriter<Customer> = ItemWriter { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(10)
            .reader(pagingCustomerItemReader(dataSource, null, null))
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
    runApplication<JdbcJob>(*args)
}
