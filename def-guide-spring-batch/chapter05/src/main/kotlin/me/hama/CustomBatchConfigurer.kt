package me.hama

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

//class CustomBatchConfigurer(
//    @Qualifier("repositoryDatasource")
//    private val datasource: DataSource,
//    @Qualifier("batchTransactionManager")
//    private val transactionManager: PlatformTransactionManager
//) : DefaultBatchConfigurer() {
//
//    override fun createJobRepository(): JobRepository {
//        val factoryBean = JobRepositoryFactoryBean()
//        factoryBean.setDatabaseType(DatabaseType.MYSQL.productName)
//        factoryBean.setTablePrefix("FOO_")
//        factoryBean.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ")
//        factoryBean.setDataSource(datasource)
//        factoryBean.afterPropertiesSet()
//        return factoryBean.`object`
//    }
//
//    override fun getTransactionManager(): PlatformTransactionManager {
//        return transactionManager
//    }
//
//    override fun getJobExplorer(): JobExplorer {
//        val factoryBean = JobExplorerFactoryBean()
//        factoryBean.setDataSource(datasource)
//        factoryBean.setTablePrefix("FOO_")
//        factoryBean.afterPropertiesSet()
//        return factoryBean.`object`
//    }
//}
