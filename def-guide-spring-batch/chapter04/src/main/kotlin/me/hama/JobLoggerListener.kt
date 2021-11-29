package me.hama

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

class JobLoggerListener : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution) {
        println("${jobExecution.jobInstance.jobName} is beginning execution")
    }

    override fun afterJob(jobExecution: JobExecution) {
        println("${jobExecution.jobInstance.jobName} has completed with the status ${jobExecution.status}")
    }
}
