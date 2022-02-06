package me.hama.database.repository

import me.hama.database.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository: JpaRepository<Customer, Long> {
    fun findByCity(city: String, pageRequest: Pageable): Page<Customer>
}
