package me.hama.transaction.batch

import me.hama.transaction.domain.AccountSummary
import me.hama.transaction.domain.TransactionDao
import org.springframework.batch.item.ItemProcessor

class TransactionApplierProcessor(
    private val transactionDao: TransactionDao
) : ItemProcessor<AccountSummary, AccountSummary> {
    override fun process(item: AccountSummary): AccountSummary {
        val transactions = transactionDao.getTransactionsByAccountNumber(item.accountNumber)
        item.currentBalance -= transactions.sumOf { it.amount }
        return item
    }
}
