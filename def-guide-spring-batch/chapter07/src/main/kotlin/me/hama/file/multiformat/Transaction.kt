package me.hama.file.multiformat

import java.util.*

data class Transaction (
    var accountNumber: String,
    var transactionDate: Date,
    var amount: Double
)
