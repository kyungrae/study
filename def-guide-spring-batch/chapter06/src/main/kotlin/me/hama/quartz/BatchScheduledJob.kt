package me.hama.quartz

import org.quartz.JobExecutionContext
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.quartz.QuartzJobBean

class BatchScheduledJob(
    private val job: Job,
    private val jobExplorer: JobExplorer,
    private val jobLauncher: JobLauncher,
) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        val jobParameters = JobParametersBuilder(jobExplorer)
            .getNextJobParameters(job)
            .toJobParameters()

        try {
            jobLauncher.run(job, jobParameters)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
