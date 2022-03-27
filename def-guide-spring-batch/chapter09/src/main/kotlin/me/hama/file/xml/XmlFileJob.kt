package me.hama.file.xml

import me.hama.file.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.oxm.xstream.XStreamMarshaller

@SpringBootApplication
@EnableBatchProcessing
class XmlFileJob(
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilderFactory: JobBuilderFactory
) {
    @Bean
    @StepScope
    fun customerItemReader(
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
    fun xmlCustomerWriter(
        @Value("#{jobParameters['outputFile']}") outputFile: Resource?
    ): StaxEventItemWriter<Customer> {
        val aliases = HashMap<String, Class<*>>()
        aliases["customer"] = Customer::class.java

        val marshaller = XStreamMarshaller()
        marshaller.setAliases(aliases)
        marshaller.afterPropertiesSet()

        return StaxEventItemWriterBuilder<Customer>()
            .name("customerFileReader")
            .resource(outputFile!!)
            .marshaller(marshaller)
            .rootTagName("customers")
            .build()
    }

    @Bean
    fun xmlFormatStep(): Step {
        return stepBuilderFactory["xmlFormatStep"]
            .chunk<Customer, Customer>(10)
            .reader(customerItemReader(null))
            .writer(xmlCustomerWriter(null))
            .build()
    }

    @Bean
    fun xmlFormatJob(): Job {
        return jobBuilderFactory["xmlFormatJob"]
            .start(xmlFormatStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<XmlFileJob>(*args)
}
