package me.hama.rest

import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import java.util.*

data class JobLauncherRequest(
    val name: String,
    val jobParameters: Properties
) {
    fun getJobParameters(): JobParameters {
        val properties = Properties()
        properties.putAll(jobParameters)
        return JobParametersBuilder(properties)
            .toJobParameters()
    }
}
