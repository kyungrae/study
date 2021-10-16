package me.hama

import org.springframework.data.repository.CrudRepository
import reactor.core.publisher.Mono

interface UserRepository: CrudRepository<User, String> {
    fun findByName(name: String): Mono<User>
}
