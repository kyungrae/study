package me.hama.file.delimit

import org.springframework.batch.item.file.transform.DefaultFieldSetFactory
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.batch.item.file.transform.LineTokenizer

class CustomerFileLineTokenizer : LineTokenizer {
    private val delimiter: String = ","
    private val names = arrayOf("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode")
    private val fieldSetFactory = DefaultFieldSetFactory()

    override fun tokenize(line: String?): FieldSet {
        val fields = line?.split(delimiter) ?: emptyList()

        val parsedFields = arrayListOf<String>()
        for (i in 0..fields.size) {
            if (i == 4)
                parsedFields[i - 1] = fields[i - 1] + " " + fields[i]
            else
                parsedFields.add(fields[i])
        }

        return fieldSetFactory.create(parsedFields.toTypedArray(), names)
    }
}
