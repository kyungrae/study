package me.hama

class UpperCaseNameService {
    fun upperCase(customer: Customer): Customer {
        return Customer(
            firstName = customer.firstName.uppercase(),
            middleInitial = customer.middleInitial?.uppercase(),
            lastName = customer.lastName.uppercase(),
            address = customer.address,
            city = customer.city,
            state = customer.state,
            zip = customer.zip
        )
    }
}
