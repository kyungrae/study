package me.hama.rest

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@EnableBatchProcessing
@SpringBootApplication
class RestApplication(
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
            println("step 1 ran today!")
            RepeatStatus.FINISHED
        }.build()
}

fun main(args: Array<String>) {
    runApplication<RestApplication>(*args)
}
