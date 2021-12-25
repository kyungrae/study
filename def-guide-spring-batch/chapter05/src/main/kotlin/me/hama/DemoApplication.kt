package me.hama

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@EnableBatchProcessing
@SpringBootApplication
class DemoApplication(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobExplorer: JobExplorer
) {
    @Bean
    fun explorerTasklet(): Tasklet {
        return ExploringTasklet(jobExplorer)
    }

    @Bean
    fun explorerStep(): Step {
        return stepBuilderFactory["explorerStep"]
            .tasklet(explorerTasklet())
            .build()
    }

    @Bean
    fun explorerJob(): Job {
        return jobBuilderFactory["explorerJob"]
            .start(explorerStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
