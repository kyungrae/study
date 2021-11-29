package me.hama

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

class HelloWorldTasklet : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val name = chunkContext.stepContext
            .jobParameters["name"] as String

        val jobExecutionContext = chunkContext.stepContext
            .stepExecution
            .executionContext

        jobExecutionContext.put("user.name", name)
        println("Hello, $name")
        return RepeatStatus.FINISHED
    }
}
