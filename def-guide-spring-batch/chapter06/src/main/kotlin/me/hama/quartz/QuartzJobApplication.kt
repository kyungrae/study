package me.hama.quartz

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
class QuartzJobApplication(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun job(): Job = jobBuilderFactory["job"]
        .incrementer(RunIdIncrementer())
        .start(step1())
        .build()

    @Bean
    fun step1(): Step = stepBuilderFactory["step1"]
        .tasklet { contribution, chunkContext ->
            println("step1 ran!")
            RepeatStatus.FINISHED
        }.build()
}

fun main(args:Array<String>) {
    val application = SpringApplication(QuartzJobApplication::class.java)
    application.webApplicationType = WebApplicationType.NONE
    application.run(*args)
}
