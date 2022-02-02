package me.hama.file.xml

import me.hama.config.NoArg
import java.util.*

@NoArg
class Transaction(
    var accountNumber: String,
    var transactionDate: Date,
    var amount: Double
)
