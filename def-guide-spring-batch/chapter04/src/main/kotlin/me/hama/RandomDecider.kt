package me.hama

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import kotlin.random.Random

class RandomDecider : JobExecutionDecider {
    private val random = Random(1)

    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        return if (random.nextBoolean()) FlowExecutionStatus(FlowExecutionStatus.COMPLETED.name)
        else FlowExecutionStatus(FlowExecutionStatus.FAILED.name)
    }
}
