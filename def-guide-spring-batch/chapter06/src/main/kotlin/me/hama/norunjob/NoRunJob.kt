package me.hama.norunjob

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.*

@SpringBootApplication
@EnableBatchProcessing
class NoRunJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun job(): Job = jobBuilderFactory["job"]
        .start(step1())
        .build()

    @Bean
    fun step1(): Step = stepBuilderFactory["step1"]
        .tasklet { contribution, chunkContext ->
            println("step1 ran!")
            RepeatStatus.FINISHED
        }.build()
}

fun main(args: Array<String>) {
    val application = SpringApplication(NoRunJob::class.java)

    val properties = Properties()
    properties["spring.batch.job.enabled"] = false
    application.setDefaultProperties(properties)
    application.webApplicationType = WebApplicationType.NONE

    application.run(*args)
}
