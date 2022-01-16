package me.hama.file.multiformat

import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.FieldSet

class TransactionFieldSetMapper: FieldSetMapper<Any> {

    override fun mapFieldSet(fieldSet: FieldSet): Any {
        return Transaction(
            fieldSet.readString("accountNumber"),
            fieldSet.readDate("transactionDate", "yyyy-MM-dd HH:mm:ss"),
            fieldSet.readDouble("amount")
        )
    }
}
