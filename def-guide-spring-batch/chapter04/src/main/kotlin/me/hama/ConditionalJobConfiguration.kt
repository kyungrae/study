package me.hama

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
class ConditionalJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    fun passTasklet() = Tasklet { contribution, chunkContext ->
        throw RuntimeException("Causing a failure")
        RepeatStatus.FINISHED
    }

    @Bean
    fun successTasklet() = Tasklet { contribution, chunkContext ->
        println("Success!")
        RepeatStatus.FINISHED
    }

    @Bean
    fun failTasklet() = Tasklet { contribution, chunkContext ->
        println("Failure")
        RepeatStatus.FINISHED
    }

//    @Bean
//    fun conditionalJob() = jobBuilderFactory["conditionalJob"]
//        .start(firstStep())
//        .on("FAILED").to(failureStep())
//        .from(firstStep()).on("*").to(successStep())
//        .end()
//        .build()

//    @Bean
//    fun randomConditionalJob() = jobBuilderFactory["randomConditionalJob"]
//        .start(firstStep())
//        .next(decider())
//        .from(decider())
//        .on("FAILED").to(failureStep())
//        .from(decider())
//        .on("*").to(successStep())
//        .end()
//        .build()

//    @Bean
//    fun completedJob() = jobBuilderFactory["completedJob"]
//        .start(firstStep())
//        .on("FAILED").end()
//        .from(firstStep()).on("*").to(successStep())
//        .end()
//        .build()

//    @Bean
//    fun failedJob() = jobBuilderFactory["failedJob"]
//        .start(firstStep())
//        .on("FAILED").fail()
//        .from(firstStep()).on("*").to(successStep())
//        .end()
//        .build()

    @Bean
    fun stopJob() = jobBuilderFactory["stopJob"]
        .start(firstStep())
        .on("FAILED").stopAndRestart(successStep())
        .from(firstStep()).on("*").to(successStep())
        .end()
        .build()

    @Bean
    fun firstStep() = stepBuilderFactory["firstStep"]
        .tasklet(passTasklet())
        .build()

    @Bean
    fun successStep() = stepBuilderFactory["successStep"]
        .tasklet(successTasklet())
        .build()

    @Bean
    fun failureStep() = stepBuilderFactory["failureStep"]
        .tasklet(failTasklet())
        .build()

    @Bean
    fun decider() = RandomDecider()
}
