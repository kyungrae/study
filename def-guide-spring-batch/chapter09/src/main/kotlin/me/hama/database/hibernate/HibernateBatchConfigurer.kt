package me.hama.database.hibernate

import org.hibernate.SessionFactory
import org.springframework.batch.core.configuration.annotation.BatchConfigurer
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.hibernate5.HibernateTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
class HibernateBatchConfigurer(
    entityManagerFactory: EntityManagerFactory,
    private val dataSource: DataSource
) : BatchConfigurer {

    private val sessionFactory: SessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
    private val transactionManager = HibernateTransactionManager(sessionFactory)

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

    @Bean
    private fun createRepository(): JobRepository {
        val jobRepositoryFactoryBean = JobRepositoryFactoryBean()
        jobRepositoryFactoryBean.setDataSource(dataSource)
        jobRepositoryFactoryBean.transactionManager = transactionManager

        jobRepositoryFactoryBean.afterPropertiesSet()

        return jobRepositoryFactoryBean.`object`
    }

    @Bean
    private fun createExplorer(): JobExplorer {
        val jobExplorerFactoryBean = JobExplorerFactoryBean()
        jobExplorerFactoryBean.setDataSource(dataSource)
        jobExplorerFactoryBean.afterPropertiesSet()
        return jobExplorerFactoryBean.`object`
    }

    @Bean
    private fun createLauncher(): JobLauncher {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}
