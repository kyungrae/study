package me.hama

import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.listener.ExecutionContextPromotionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
class ExecutionContextConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    fun job() = jobBuilderFactory["job"]
        .start(step())
        .build()

    @Bean
    fun step() = stepBuilderFactory["step"]
        .tasklet(HelloWorldTasklet())
        .build()

    @Bean
    fun executionContextPromotionJob() = jobBuilderFactory["executionContextPromotionJob"]
        .start(step1())
        .next(step2())
        .build()

    @Bean
    fun step1() = stepBuilderFactory["step1"]
        .tasklet(HelloWorldTasklet())
        .listener(promotionListener())
        .build()

    @Bean
    fun step2() = stepBuilderFactory["step2"]
        .tasklet(HelloWorldTasklet())
        .build()

    @Bean
    fun promotionListener(): StepExecutionListener {
        val listener = ExecutionContextPromotionListener()
        listener.setKeys(arrayOf("name"))
        return listener
    }
}
