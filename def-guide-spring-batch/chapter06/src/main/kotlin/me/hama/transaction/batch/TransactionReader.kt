package me.hama.transaction.batch

import me.hama.transaction.domain.Transaction
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.file.transform.FieldSet

open class TransactionReader(
    private val fieldSetReader: ItemStreamReader<FieldSet>
) : ItemStreamReader<Transaction> {
    private var recordCount: Int = 0
    private var expectedRecordCount: Int = 0
    private lateinit var stepExecution: StepExecution

    override fun open(executionContext: ExecutionContext) {
        fieldSetReader.open(executionContext)
    }

    override fun update(executionContext: ExecutionContext) {
        fieldSetReader.update(executionContext)
    }

    override fun close() {
        fieldSetReader.close()
    }

    override fun read(): Transaction? {
//        if (recordCount == 25) throw ParseException("This isn't what I hoped to happen.")
        return process(fieldSetReader.read())
    }

    private fun process(fieldSet: FieldSet?): Transaction? {
        var result: Transaction? = null

        if (fieldSet != null) {
            if (fieldSet.fieldCount > 1) {
                result = Transaction(
                    accountNumber = fieldSet.readString(0),
                    timestamp = fieldSet.readDate(1, "yyyy-MM-DD HH:mm:ss"),
                    amount = fieldSet.readDouble(2)
                )
                recordCount++
            } else {
                expectedRecordCount = fieldSet.readInt(0)

                if (recordCount != expectedRecordCount) {
                    stepExecution.setTerminateOnly()
                }
            }
        }
        return result
    }

    @BeforeStep
    fun beforeStep(execution: StepExecution) {
        this.stepExecution = execution
    }

//    @AfterStep
//    fun afterStep(execution: StepExecution): ExitStatus {
//        return if (recordCount == expectedRecordCount) execution.exitStatus
//        else ExitStatus.STOPPED
//    }
}
