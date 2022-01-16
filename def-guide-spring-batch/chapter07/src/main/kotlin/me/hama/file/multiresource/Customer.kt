package me.hama.file.multiresource

import me.hama.config.NoArg

@NoArg
data class Customer(
    var firstName: String,
    var middleInitial: String,
    var lastName: String,
    var address: String,
    var city: String,
    var state: String,
    var zipCode: String,
    var transactions: MutableList<Transaction>
)
