package me.hama.file.fix

import me.hama.config.NoArg

@NoArg
data class Customer(
    var firstName: String,
    var middleInitial: String,
    var lastName: String,
    var addressNumber: String,
    var street: String,
    var city: String,
    var state: String,
    var zipCode: String
)
