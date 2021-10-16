package me.hama

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.hateoas.MediaTypes
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@WebFluxTest(controllers = [HypermediaItemController::class])
@AutoConfigureRestDocs
internal class HypermediaItemControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: InventoryService

    @MockBean
    private lateinit var repository: ItemRepository

    @Test
    @WithMockUser(username = "hama", roles = ["USER_ROLE"])
    fun findingAllItems() {
        `when`(repository.findAll()).thenReturn(
            Flux.just(Item("item-1", "Alf alarm clock", "nothing I really need", 19.99))
        )
        `when`(repository.findById(anyString())).thenReturn(
            Mono.just(Item("item-1", "Alf alarm clock", "nothing I really need", 19.99))
        )

        webTestClient.get().uri("/hypermedia/items")
            .accept(MediaTypes.HAL_FORMS_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(document("findAll-hypermedia", preprocessResponse(prettyPrint())))
    }

    @Test
    @WithMockUser(username = "hama", roles = ["USER_ROLE"])
    fun findOneItem() {
        `when`(repository.findById("item-1")).thenReturn(
            Mono.just(Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)))

        webTestClient.get().uri("/hypermedia/items/item-1")
            .accept(MediaTypes.HAL_FORMS_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(document("findOne-hypermedia", preprocessResponse(prettyPrint())))
    }

    @Test
    @WithMockUser(username = "hama", roles = ["USER_ROLE"])
    fun findProfile() {
        webTestClient.get().uri("/hypermedia/items/profile")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(document("profile", preprocessResponse(prettyPrint())))
    }
}
