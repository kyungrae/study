package me.hama.file.xml

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.xml.StaxEventItemReader
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.oxm.jaxb.Jaxb2Marshaller

@SpringBootApplication
@EnableBatchProcessing
class XmlJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun customerFileReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource?
    ): StaxEventItemReader<Customer> {
        return StaxEventItemReaderBuilder<Customer>()
            .name("customerFileReader")
            .resource(inputFile!!)
            .addFragmentRootElements("customer")
            .unmarshaller(customerMarshaller())
            .build()
    }

    @Bean
    fun customerMarshaller(): Jaxb2Marshaller {
        val jaxb2Marshaller = Jaxb2Marshaller()
        jaxb2Marshaller.setClassesToBeBound(Customer::class.java, Transaction::class.java)
        return jaxb2Marshaller
    }

    @Bean
    fun itemWriter(): ItemWriter<Customer> = ItemWriter { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Customer,Customer>(10)
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
    runApplication<XmlJob>(*args)
}
