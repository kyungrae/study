package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.test.StepVerifier

@SpringBootTest
@AutoConfigureWebTestClient
class RSocketTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var repository: ItemRepository

    @Test
    fun verifyRemoteOperationsThroughRSocketRequestResponse() {
        repository.deleteAll()
            .`as`(StepVerifier::create)
            .verifyComplete()

        webTestClient.post().uri("/items/request-response")
            .bodyValue(Item(name = "Alf alarm clock", description = "nothing important", price = 19.99))
            .exchange()
            .expectStatus().isCreated
            .expectBody<Item>()
            .consumeWith {
                assertThat(it.responseBody?.id).isNotNull
                assertThat(it.responseBody?.name).isEqualTo("Alf alarm clock")
                assertThat(it.responseBody?.description).isEqualTo("nothing important")
                assertThat(it.responseBody?.price).isEqualTo(19.99)
            }

        Thread.sleep(500)

        repository.findAll()
            .`as`(StepVerifier::create)
            .expectNextMatches {
                assertThat(it.id).isNotNull
                assertThat(it.name).isEqualTo("Alf alarm clock")
                assertThat(it.description).isEqualTo("nothing important")
                assertThat(it.price).isEqualTo(19.99)
                true
            }
            .verifyComplete()
    }

    @Test
    fun verifyRemoteOperationsThroughRSocketRequestStream() {
        repository.deleteAll().block()
        val items = List(3) { Item(name = "name - $it", description = "description - $it", price = it.toDouble()) }
        repository.saveAll(items).blockLast()

        webTestClient.get().uri("/items/request-stream")
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus().isOk
            .returnResult(Item::class.java)
            .responseBody
            .`as`(StepVerifier::create)
            .expectNextMatches(itemPredicate("0"))
            .expectNextMatches(itemPredicate("1"))
            .expectNextMatches(itemPredicate("2"))
            .verifyComplete()
    }

    @Test
    @Throws(InterruptedException::class)
    fun verifyRemoteOperationsThroughRSocketFireAndForget() {
        repository.deleteAll()
            .`as`(StepVerifier::create)
            .verifyComplete()


        webTestClient.post().uri("/items/fire-and-forget")
            .bodyValue(Item(name = "Alf alarm clock", description = "nothing important", price =  19.99))
            .exchange()
            .expectStatus().isCreated
            .expectBody().isEmpty

        Thread.sleep(500)

        repository.findAll()
            .`as`(StepVerifier::create)
            .expectNextMatches { item: Item ->
                assertThat(item.id).isNotNull
                assertThat(item.name).isEqualTo("Alf alarm clock")
                assertThat(item.description).isEqualTo("nothing important")
                assertThat(item.price).isEqualTo(19.99)
                true
            }
            .verifyComplete()
    }

    private fun itemPredicate(num: String) = { item: Item ->
        assertThat(item.name).startsWith("name")
        assertThat(item.name).endsWith(num)
        assertThat(item.description).startsWith("description")
        assertThat(item.description).endsWith(num)
        assertThat(item.price).isGreaterThanOrEqualTo(0.0)
        true
    }
}
