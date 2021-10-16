package me.hama

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ItemRepository : ReactiveCrudRepository<Item, String>, ReactiveQueryByExampleExecutor<Item> {
    fun findByNameContaining(partialName: String): Flux<Item>

    fun findByNameContainingIgnoreCase(partialName: String): Flux<Item>

    fun findByDescriptionContainingIgnoreCase(partialDesc: String): Flux<Item>

    fun findByNameContainingAndDescriptionContainingAllIgnoreCase(partialName: String, partialDesc: String): Flux<Item>

    fun findByNameContainingOrDescriptionContainingAllIgnoreCase(partialName: String, partialDesc: String): Flux<Item>
}
