package me.hama

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

class ExploringTasklet(
    private val explorer: JobExplorer
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val jobName = chunkContext.stepContext.jobName
        val instances = explorer.getJobInstances(jobName, 0, Int.MAX_VALUE)

        println("There are ${instances.size} job instances for the job $jobName")
        println("They have had the following results")
        println("************************************")

        for (instance in instances) {
            val jobExecutions = explorer.getJobExecutions(instance)

            println("Instance ${instance.instanceId} had ${jobExecutions.size} executions")
            for (jobExecution in jobExecutions)
                println("\tExecution ${jobExecution.id} resulted in Exit Status ${jobExecution.exitStatus}")
        }
        return RepeatStatus.FINISHED
    }
}
