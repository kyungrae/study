package me.hama.database.hibernate

import org.hibernate.SessionFactory
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.orm.hibernate5.HibernateTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
class HibernateBatchConfigurer(
    entityManagerFactory: EntityManagerFactory,
    dataSource: DataSource
) : DefaultBatchConfigurer(dataSource) {

    private val sessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
    private var transactionManager: PlatformTransactionManager = HibernateTransactionManager(sessionFactory)

    override fun getTransactionManager(): PlatformTransactionManager {
        return transactionManager
    }
}
