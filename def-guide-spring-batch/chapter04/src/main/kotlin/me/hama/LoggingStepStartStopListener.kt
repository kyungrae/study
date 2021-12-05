package me.hama

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep

class LoggingStepStartStopListener {

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        println("${stepExecution.stepName} has begun!")
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        println("${stepExecution.stepName} has ended!")
        return stepExecution.exitStatus
    }
}
