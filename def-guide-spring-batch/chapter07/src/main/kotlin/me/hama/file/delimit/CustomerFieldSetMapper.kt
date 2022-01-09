package me.hama.file.delimit

import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.FieldSet

class CustomerFieldSetMapper : FieldSetMapper<Customer> {
    override fun mapFieldSet(fieldSet: FieldSet): Customer {
        return Customer(
            firstName = fieldSet.readString("firstName"),
            middleInitial = fieldSet.readString("middleInitial"),
            lastName = fieldSet.readString("lastName"),
            address = fieldSet.readString("addressNumber") + fieldSet.readString("street"),
            city = fieldSet.readString("city"),
            state = fieldSet.readString("state"),
            zipCode = fieldSet.readString("zipCode")
        )
    }
}
