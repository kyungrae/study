package me.hama

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamSupport
import org.springframework.batch.item.validator.ValidationException
import org.springframework.batch.item.validator.Validator

class UniqueLastNameValidator : ItemStreamSupport(), Validator<Customer> {
    private var lastNames: MutableSet<String> = mutableSetOf()

    override fun validate(value: Customer) {
        if (lastNames.contains(value.lastName))
            throw ValidationException("Duplicate last name was food: ${value.lastName}")
        this.lastNames += value.lastName
    }

    override fun open(executionContext: ExecutionContext) {
        val lastNames = getExecutionContextKey("lastNames")

        if (executionContext.containsKey(lastNames)) {
            this.lastNames = executionContext[lastNames] as MutableSet<String>
        }
    }

    override fun update(executionContext: ExecutionContext) {
        val itr = lastNames.iterator()
        val copiedLastNames = mutableSetOf<String>()
        while (itr.hasNext())
            copiedLastNames += itr.next()
        executionContext.put(getExecutionContextKey("lastName"), copiedLastNames)
    }
}
