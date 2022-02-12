package me.hama.database

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution

class EmptyInputStepFailer {
    fun afterStep(execution: StepExecution): ExitStatus {
        return if (execution.readCount > 0)
            execution.exitStatus
        else
            ExitStatus.FAILED
    }
}
