package me.hama

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@EnableBatchProcessing
@SpringBootApplication
class HelloWorldJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun job(): Job =
        jobBuilderFactory["basicJob"]
            .start(step1())
            .build()

    @Bean
    fun step1(): Step =
        stepBuilderFactory["step1"]
            .tasklet(helloWorldTasklet())
            .build()

    @Bean
    fun helloWorldTasklet() = Tasklet { contribution, chunkContext ->
        val name = chunkContext.stepContext
            .jobParameters["name"] as String
        println("Hello, $name!")
        RepeatStatus.FINISHED
    }

    @StepScope
    @Bean
    fun helloWorldTasklet(@Value("#{jobParameters['name']}") name: String?) = Tasklet { contribution, chunkContext ->
        println("Hello, $name!")
        RepeatStatus.FINISHED
    }
}

fun main(args: Array<String>) {
    runApplication<HelloWorldJob>(*args)
}
