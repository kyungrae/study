package me.hama.file.json

import me.hama.config.NoArg
import java.util.*

@NoArg
class Transaction(
    var accountNumber: String,
    var transactionDate: Date,
    var amount: Double
)
