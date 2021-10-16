package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
class LoadingWebSiteIntegrationTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    @WithMockUser(username = "hama", roles = ["USER"])
    fun test() {
        client.get().uri("/").exchange()
            .expectStatus().isOk
            .expectHeader().contentType(TEXT_HTML)
            .expectBody<String>()
            .consumeWith { exchangeResult ->
                assertThat(exchangeResult.responseBody).contains("<h1>Welcome to Hacking with Spring Boot!</h1>")
            }
    }
}
