package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import reactor.test.StepVerifier

@DataMongoTest
class MongoDbSliceTest {

    @Autowired
    lateinit var repository: ItemRepository

    @Test
    fun itemRepositorySavesItems() {
        val sampleItem = Item(name = "name", description = "description", price = 1.99)

        repository.save(sampleItem)
            .`as`(StepVerifier::create)
            .expectNextMatches { item ->
                assertThat(item.id).isNotNull
                assertThat(item.name).isEqualTo("name")
                assertThat(item.description).isEqualTo("description")
                assertThat(item.price).isEqualTo(1.99)
                true
            }
    }
}
