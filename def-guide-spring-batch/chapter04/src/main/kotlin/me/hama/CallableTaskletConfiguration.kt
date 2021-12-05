package me.hama

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Callable

//@Configuration
class CallableTaskletConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun callableJob(): Job =
        jobBuilderFactory["callableJob"]
            .start(callableStep())
            .build()

    @Bean
    fun callableStep(): Step =
        stepBuilderFactory["callableStep"]
            .tasklet(tasklet())
            .build()

    @Bean
    fun tasklet(): CallableTaskletAdapter {
        val callableTaskletAdapter = CallableTaskletAdapter()
        callableTaskletAdapter.setCallable(callableObject())
        return callableTaskletAdapter
    }

    @Bean
    fun callableObject() = Callable {
        println("This was executed in another thread")
        RepeatStatus.FINISHED
    }
}
