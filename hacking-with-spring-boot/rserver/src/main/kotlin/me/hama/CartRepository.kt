package me.hama

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface CartRepository: ReactiveCrudRepository<Cart, String>
