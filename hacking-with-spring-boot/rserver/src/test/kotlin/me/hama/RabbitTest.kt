package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
class RabbitTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var repository: ItemRepository

    @Test
    @WithMockUser(username = "hama", roles = ["INVENTORY"])
    fun verifyMessagingThroughAmqp() {
        webTestClient.post().uri("/items")
            .bodyValue(Item(name = "Alf alarm clock", description = "nothing important", price = 19.99))
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        webTestClient.post().uri("/items")
            .bodyValue(Item(name = "Smurf TV tray", description = "nothing important", price = 29.99))
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        Thread.sleep(1000L)

        repository.findAll()
            .`as`(StepVerifier::create)
            .expectNextMatches { item ->
                assertThat(item.name).isEqualTo("Alf alarm clock")
                assertThat(item.description).isEqualTo("nothing important")
                assertThat(item.price).isEqualTo(19.99)
                true
            }
            .expectNextMatches { item ->
                assertThat(item.name).isEqualTo("Smurf TV tray")
                assertThat(item.description).isEqualTo("nothing important")
                assertThat(item.price).isEqualTo(29.99)
                true
            }
            .verifyComplete()
    }

    companion object {
        @Container
        val container = RabbitMQContainer("rabbitmq:3.7.25-management-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.host", container::getContainerIpAddress)
            registry.add("spring.rabbitmq.port", container::getAmqpPort)
        }
    }
}
