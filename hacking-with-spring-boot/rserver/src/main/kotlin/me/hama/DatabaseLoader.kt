package me.hama

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.stereotype.Component

@Component
class DatabaseLoader {

//    @Bean
//    fun initialize(mongo: ReactiveMongoOperations) = CommandLineRunner {
//        mongo.save(Item(name = "Alf alarm clock", description = "kids clock", price = 11.99)).subscribe()
//        mongo.save(Item(name = "Smurf TV tray", description = "kids TV tray", price = 24.99)).subscribe()
//        mongo.save(Cart("My Cart")).subscribe()
//    }
}
