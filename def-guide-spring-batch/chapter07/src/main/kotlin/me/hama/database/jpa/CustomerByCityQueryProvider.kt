package me.hama.database.jpa

import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider
import javax.persistence.Query

class CustomerByCityQueryProvider(
    private val cityName: String?
) : AbstractJpaQueryProvider() {
    override fun createQuery(): Query {
        val query = entityManager.createQuery("select c from Customer c where c.city = :city")
        query.setParameter("city", cityName)
        return query
    }

    override fun afterPropertiesSet() {
        checkNotNull(cityName)
    }
}
