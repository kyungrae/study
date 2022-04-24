package me.hama.database.jpa

import org.springframework.batch.core.configuration.annotation.BatchConfigurer
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
class JpaBatchConfigurer(
    entityManagerFactory: EntityManagerFactory,
    private val dataSource: DataSource
) : BatchConfigurer {

    private val transactionManager = JpaTransactionManager(entityManagerFactory)

    override fun getJobRepository(): JobRepository {
        return createRepository()
    }

    override fun getTransactionManager(): PlatformTransactionManager {
        return transactionManager
    }

    override fun getJobLauncher(): JobLauncher {
        return createLauncher()
    }

    override fun getJobExplorer(): JobExplorer {
        return createExplorer()
    }

    private fun createRepository(): JobRepository {
        val jobRepositoryFactoryBean = JobRepositoryFactoryBean()

        jobRepositoryFactoryBean.setDataSource(dataSource)
        jobRepositoryFactoryBean.transactionManager = transactionManager

        jobRepositoryFactoryBean.afterPropertiesSet()

        return jobRepositoryFactoryBean.`object`
    }

    private fun createExplorer(): JobExplorer {
        val jobExplorerFactoryBean = JobExplorerFactoryBean()
        jobExplorerFactoryBean.setDataSource(dataSource)
        jobExplorerFactoryBean.afterPropertiesSet()
        return jobExplorerFactoryBean.`object`
    }

    private fun createLauncher(): JobLauncher {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}
