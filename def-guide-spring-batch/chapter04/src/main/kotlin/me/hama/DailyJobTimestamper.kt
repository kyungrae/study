package me.hama

import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.JobParametersIncrementer
import java.util.*

class DailyJobTimestamper : JobParametersIncrementer {
    override fun getNext(parameters: JobParameters?): JobParameters {
        val params = parameters ?: JobParameters()
        return JobParametersBuilder(params)
            .addDate("currentDate", Date())
            .toJobParameters()
    }
}
