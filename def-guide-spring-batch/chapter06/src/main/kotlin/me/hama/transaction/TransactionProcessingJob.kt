package me.hama.transaction

import me.hama.transaction.batch.TransactionApplierProcessor
import me.hama.transaction.batch.TransactionReader
import me.hama.transaction.domain.AccountSummary
import me.hama.transaction.domain.Transaction
import me.hama.transaction.domain.TransactionDao
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import javax.sql.DataSource

@EnableBatchProcessing
@SpringBootApplication
class TransactionProcessingJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val dataSource: DataSource
) {
    @Bean
    @StepScope
    fun transactionReader(): TransactionReader =
        TransactionReader(fileItemReader(null))

    @Bean
    @StepScope
    fun fileItemReader(
        @Value("#{jobParameters['transactionFile']}") inputFile: Resource?
    ): FlatFileItemReader<FieldSet> {
        return FlatFileItemReaderBuilder<FieldSet>()
            .name("fileItemReader")
            .resource(inputFile!!)
            .lineTokenizer(DelimitedLineTokenizer())
            .fieldSetMapper(PassThroughFieldSetMapper())
            .build()
    }

    @Bean
    fun transactionWriter(): JdbcBatchItemWriter<Transaction> {
        return JdbcBatchItemWriterBuilder<Transaction>()
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .sql(
                "INSERT INTO TRANSACTION (ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT) " +
                        "VALUES ((SELECT ID FROM ACCOUNT_SUMMARY WHERE ACCOUNT_NUMBER = :accountNumber), :timestamp, :amount)"
            )
            .dataSource(dataSource)
            .build()
    }

    @Bean
    fun importTransactionFileStep(): Step {
        return stepBuilderFactory["importTransactionFileStep"]
            .startLimit(2)
            .chunk<Transaction, Transaction>(5)
            .reader(transactionReader())
            .writer(transactionWriter())
            .allowStartIfComplete(true)
            .listener(transactionReader())
            .build()
    }

    @Bean
    @StepScope
    fun accountSummaryReader(): JdbcCursorItemReader<AccountSummary> {
        return JdbcCursorItemReaderBuilder<AccountSummary>()
            .name("accountSummaryReader")
            .dataSource(dataSource)
            .sql(
                "SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE FROM ACCOUNT_SUMMARY A " +
                        "WHERE A.ID IN (SELECT DISTINCT T.ACCOUNT_SUMMARY_ID FROM TRANSACTION T) " +
                        "ORDER BY A.ACCOUNT_NUMBER"
            )
            .rowMapper { rs, rowNum ->
                AccountSummary(
                    rs.getInt("id"),
                    rs.getString("account_number"),
                    rs.getDouble("current_balance")
                )
            }
            .build()
    }

    @Bean
    fun transactionDao(): TransactionDao {
        return TransactionDao(dataSource)
    }

    @Bean
    fun transactionApplierProcessor(): TransactionApplierProcessor {
        return TransactionApplierProcessor(transactionDao())
    }

    @Bean
    fun accountSummaryWriter(): JdbcBatchItemWriter<AccountSummary> {
        return JdbcBatchItemWriterBuilder<AccountSummary>()
            .dataSource(dataSource)
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .sql("UPDATE ACCOUNT_SUMMARY SET CURRENT_BALANCE = :currentBalance WHERE ACCOUNT_NUMBER = :accountNumber")
            .build()
    }

    @Bean
    fun applyTransactionStep(): Step {
        return stepBuilderFactory["applyTransactionStep"]
            .chunk<AccountSummary, AccountSummary>(5)
            .reader(accountSummaryReader())
            .processor(transactionApplierProcessor())
            .writer(accountSummaryWriter())
            .build()
    }

    @Bean
    @StepScope
    fun accountSummaryFileWriter(@Value("#{jobParameters['summaryFile']}") summaryFile: Resource?): FlatFileItemWriter<AccountSummary> {
        val lineAggregator = DelimitedLineAggregator<AccountSummary>()
        val fieldExtractor = BeanWrapperFieldExtractor<AccountSummary>()
        fieldExtractor.setNames(arrayOf("accountNumber", "currentBalance"))
        fieldExtractor.afterPropertiesSet()
        lineAggregator.setFieldExtractor(fieldExtractor)

        return FlatFileItemWriterBuilder<AccountSummary>()
            .name("accountSummaryFileWriter")
            .resource(summaryFile!!)
            .lineAggregator(lineAggregator)
            .build()
    }

    @Bean
    fun generateAccountSummaryStep(): Step {
        return stepBuilderFactory["generateAccountSummaryStep"]
            .chunk<AccountSummary, AccountSummary>(5)
            .reader(accountSummaryReader())
            .writer(accountSummaryFileWriter(null))
            .build()
    }

    @Bean
    fun transactionJob(): Job {
        return jobBuilderFactory["transactionJob"]
            .preventRestart()
            .start(importTransactionFileStep())
//            .on("STOPPED").stopAndRestart(importTransactionFileStep())
//            .from(importTransactionFileStep()).on("*").to(applyTransactionStep())
//            .from(applyTransactionStep()).next(generateAccountSummaryStep())
//            .end()
            .next(applyTransactionStep())
            .next(generateAccountSummaryStep())
            .build()
    }
}

fun main(args: Array<String>) {
    val application = SpringApplication(TransactionProcessingJob::class.java)
    application.webApplicationType = WebApplicationType.NONE
    application.run(*args)
}
