package me.hama.file.json

import me.hama.config.NoArg

@NoArg
class Customer(
    var firstName: String,
    var middleInitial: String,
    var lastName: String,
    var address: String,
    var city: String,
    var state: String,
    var zipCode: String,
    var transactions: List<Transaction>
)
