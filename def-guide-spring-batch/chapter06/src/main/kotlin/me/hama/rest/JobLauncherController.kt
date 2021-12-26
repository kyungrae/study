package me.hama.rest

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class JobLauncherController(
    private val jobLauncher: JobLauncher,
    private val context: ApplicationContext,
    private val jobExplorer: JobExplorer
) {
    @PostMapping("/run")
    fun runJob(@RequestBody request: JobLauncherRequest): ExitStatus {
        val job = context.getBean<Job>(request.name)
        val jobParameters = JobParametersBuilder(request.getJobParameters(), jobExplorer)
            .getNextJobParameters(job)  // job.id 생성
            .toJobParameters()
        return jobLauncher.run(job, jobParameters).exitStatus
    }
}
