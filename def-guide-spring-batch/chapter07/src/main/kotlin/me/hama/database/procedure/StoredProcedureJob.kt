package me.hama.database.procedure

import me.hama.database.Customer
import me.hama.database.CustomerRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.StoredProcedureItemReader
import org.springframework.batch.item.database.builder.StoredProcedureItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter
import org.springframework.jdbc.core.SqlParameter
import java.sql.Types
import javax.sql.DataSource

@SpringBootApplication
@EnableBatchProcessing
class StoredProcedureJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerItemReader(
        dataSource: DataSource?,
        @Value("#{jobParameters['city']}") city: String?
    ): StoredProcedureItemReader<Customer> {
        return StoredProcedureItemReaderBuilder<Customer>()
            .name("customerItemReader")
            .dataSource(dataSource!!)
            .procedureName("customer_list")
            .parameters(SqlParameter("cityOption", Types.VARCHAR))
            .preparedStatementSetter(ArgumentPreparedStatementSetter(arrayOf(city)))
            .rowMapper(CustomerRowMapper())
            .build()
    }

    @Bean
    fun itemWriter(): ItemWriter<Customer> = ItemWriter { it.forEach(::println) }

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
    runApplication<StoredProcedureJob>(*args)
}
