package me.hama

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Callable

//@Configuration
class MethodInvokingTaskletConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun methodInvokingJob(): Job =
        jobBuilderFactory["methodInvokingJob"]
            .start(methodInvokingStep())
            .build()

    @Bean
    fun methodInvokingStep(): Step =
        stepBuilderFactory["methodInvokingStep"]
            .tasklet(methodInvokingTaskletAdapter(null))
            .build()

    @StepScope
    @Bean
    fun methodInvokingTaskletAdapter(
        @Value("#{jobParameters['message']}") message: String?
    ): MethodInvokingTaskletAdapter {
        val methodInvokingTaskletAdapter = MethodInvokingTaskletAdapter()
        methodInvokingTaskletAdapter.setTargetObject(service())
        methodInvokingTaskletAdapter.setTargetMethod("serviceMethod")
        methodInvokingTaskletAdapter.setArguments(arrayOf(message))
        return methodInvokingTaskletAdapter
    }

    @Bean
    fun service() = CustomService()
}
