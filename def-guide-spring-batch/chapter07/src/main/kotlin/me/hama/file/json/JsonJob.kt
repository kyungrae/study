package me.hama.file.json

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.json.JacksonJsonObjectReader
import org.springframework.batch.item.json.JsonItemReader
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import java.text.SimpleDateFormat

@SpringBootApplication
@EnableBatchProcessing
class JsonJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerFileReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): JsonItemReader<Customer> {
        val objectMapper = ObjectMapper()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

        val jsonObjectReader = JacksonJsonObjectReader(Customer::class.java)
        jsonObjectReader.setMapper(objectMapper)

        return JsonItemReaderBuilder<Customer>()
            .name("customerFileReader")
            .jsonObjectReader(jsonObjectReader)
            .resource(inputFile!!)
            .build()
    }

    @Bean
    fun itemWriter(): ItemWriter<Customer> = ItemWriter { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerFileReader(null))
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
    runApplication<JsonJob>(*args)
}
