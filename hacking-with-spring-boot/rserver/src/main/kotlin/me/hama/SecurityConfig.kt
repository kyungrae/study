package me.hama

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun userDetailsService(repository: UserRepository) = ReactiveUserDetailsService { username: String ->
        repository.findByName(username)
            .map { user ->
                User.builder().passwordEncoder(passwordEncoder()::encode)
                    .username(user.name)
                    .password(user.password)
                    .authorities(*user.roles.toTypedArray())
                    .build()
            }
    }

    @Bean
    fun myCustomSecurityPolicy(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange {
            it
                .anyExchange().authenticated()
                .and()
                .httpBasic()
                .and()
                .formLogin()
        }.csrf().disable().build()
    }

//    @Bean
//    fun userLoader(operations: ReactiveMongoOperations) = CommandLineRunner {
//        operations.save(User(name = "hama", password = "password", roles = listOf("ROLE_USER"))).subscribe()
//        operations.save(User(name = "manager", password = "password", roles = listOf("ROLE_INVENTORY"))).subscribe()
//    }
}
