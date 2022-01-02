package me.hama.transaction.domain

import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

class TransactionDao(dataSource: DataSource) : JdbcTemplate(dataSource) {

    fun getTransactionsByAccountNumber(accountNumber: String): List<Transaction> =
        query(
            "SELECT t.id, t.timestamp, t.amount " +
                    "FROM TRANSACTION t INNER JOIN ACCOUNT_SUMMARY a " +
                    "ON a.id = t.account_summary_id WHERE a.account_number = ?",
            { rs, rowNum -> Transaction(accountNumber, rs.getDate("timestamp"), rs.getDouble("amount")) },
            arrayOf(accountNumber)
        )

}
