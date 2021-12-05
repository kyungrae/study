package me.hama

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor

//@Configuration
class SystemCommandTaskletConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    fun job() = jobBuilderFactory["systemCommandJob"]
        .start(systemCommandStep1())
        .next(systemCommandStep2())
        .build()

    @Bean
    fun systemCommandStep1() = stepBuilderFactory["systemCommandStep1"]
        .tasklet(systemCommandTasklet1())
        .build()

    @Bean
    fun systemCommandTasklet1(): SystemCommandTasklet {
        val systemCommandTasklet = SystemCommandTasklet()

        systemCommandTasklet.setCommand("rm -rf /tmp.txt")
        systemCommandTasklet.setTimeout(5000)
        systemCommandTasklet.setInterruptOnCancel(true)

        return systemCommandTasklet
    }

    @Bean
    fun systemCommandStep2() = stepBuilderFactory["systemCommandStep2"]
        .tasklet(systemCommandStep())
        .build()

    @Bean
    fun systemCommandStep(): SystemCommandTasklet {
        val tasklet = SystemCommandTasklet()

        tasklet.setCommand("touch tmp.txt")
        tasklet.setTimeout(5000)
        tasklet.setInterruptOnCancel(true)

        tasklet.setWorkingDirectory("/Users/hama/workspace")

        tasklet.setSystemProcessExitCodeMapper(touchCodeMapper())
        tasklet.setTerminationCheckInterval(5000)
        tasklet.setTaskExecutor(SimpleAsyncTaskExecutor())
        tasklet.setEnvironmentParams(arrayOf("JAVA_HOME=/java", "BATCH_HOME=/Users/batch"))

        return tasklet
    }

    @Bean
    fun touchCodeMapper(): SimpleSystemProcessExitCodeMapper = SimpleSystemProcessExitCodeMapper()
}
