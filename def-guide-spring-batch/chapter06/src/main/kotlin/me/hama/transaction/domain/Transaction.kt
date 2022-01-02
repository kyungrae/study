package me.hama.transaction.domain

import java.util.*

class Transaction(
    val accountNumber: String,
    val timestamp: Date,
    val amount: Double
)
