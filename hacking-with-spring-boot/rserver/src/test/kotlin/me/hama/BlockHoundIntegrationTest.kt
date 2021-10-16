package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

@ExtendWith(SpringExtension::class)
class BlockHoundIntegrationTest {

    lateinit var inventoryService: InventoryService

    @MockBean
    lateinit var itemRepository: ItemRepository

    @MockBean
    lateinit var cartRepository: CartRepository

    @BeforeEach
    internal fun setUp() {
        val sampleItem = Item("item1", "TV tray", "Alf TV tray", 19.99)
        val sampleCartItem = CartItem(sampleItem)
        val sampleCart = Cart("My Cart", mutableListOf(sampleCartItem))

        `when`(cartRepository.findById(anyString())).thenReturn(Mono.empty<Cart>().hide())
        `when`(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem))
        `when`(cartRepository.save(any(Cart::class.java))).thenReturn(Mono.just(sampleCart))

        inventoryService = InventoryService(itemRepository, cartRepository)
    }

    @Test
    fun blockHoundShouldTrapBlockingCall() {
        Mono.delay(Duration.ofSeconds(1))
            .flatMap { inventoryService.blockingAddItemToCart("My Cart", "item1") }
            .`as`(StepVerifier::create)
            .verifyErrorSatisfies { throwable ->
                assertThat(throwable).hasMessageContaining("block()/blockFirst()/blockLast() are blocking")
            }
    }
}
