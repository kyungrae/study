package me.hama

import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class Customer(
    @field:NotNull(message = "First name is required")
    @field:Pattern(regexp = "[a-zA-Z]+", message = "First name must be alphabetical")
    var firstName: String = "",

    @field:Size(min = 1, max = 1)
    @field:Pattern(regexp = "[a-zA-Z]", message = "Middle initial must be alphabetical")
    var middleInitial: String? = null,

    @field:NotNull(message = "Last name is required")
    @field:Pattern(regexp = "[a-zA-Z]+", message = "Last name must be alphabetcial")
    var lastName: String = "",

    @field:NotNull(message = "Address is required")
    @field:Pattern(regexp = "[0-9a-zA-Z. ]+")
    var address: String = "",

    @field:NotNull(message = "City is required")
    @field:Pattern(regexp = "[0-9a-zA-Z. ]+")
    var city: String = "",

    @field:NotNull(message = "State is required")
    @field:Size(min = 2, max = 2)
    @field:Pattern(regexp = "[A-Z]{2}")
    var state: String = "",

    @field:NotNull(message = "Zip is required")
    @field:Size(min = 5, max = 5)
    @field:Pattern(regexp = "\\d{5}")
    var zip: String = ""
){
    override fun toString(): String {
        return "Customer(firstName='$firstName', middleInitial=$middleInitial, lastName='$lastName', address='$address', city='$city', state='$state', zip='$zip')"
    }
}
