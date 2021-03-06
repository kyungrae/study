package me.hama

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.CompositeJobParametersValidator
import org.springframework.batch.core.job.DefaultJobParametersValidator
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
class JobParameterConfiguration (
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    fun validator(): CompositeJobParametersValidator {
        val validator = CompositeJobParametersValidator()

        val defaultJobParametersValidator =
            DefaultJobParametersValidator(arrayOf("fileName"), arrayOf("name", "currentDate"))
        defaultJobParametersValidator.afterPropertiesSet()

        validator.setValidators(listOf(ParameterValidator(), defaultJobParametersValidator))

        return validator
    }

    @Bean
    fun job(): Job =
        jobBuilderFactory["basicJob"]
            .start(step1())
            .validator(validator())
            .incrementer(DailyJobTimestamper())
            .listener(JobLoggerListener())
            .build()

    @Bean
    fun step1(): Step =
        stepBuilderFactory["step1"]
            .tasklet(HelloWorldTasklet())
            .build()

    @StepScope
    @Bean
    fun helloWorldTasklet(
        @Value("#{jobParameters['name']}") name: String?,
        @Value("#{jobParameters['fileName']}") fileName: String?
    ) = Tasklet { contribution, chunkContext ->
        println("Hello, $name")
        println("fileName = $fileName")
        RepeatStatus.FINISHED
    }
}
