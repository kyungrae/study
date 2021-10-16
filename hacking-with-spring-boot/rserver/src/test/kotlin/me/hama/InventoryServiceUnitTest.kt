package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
internal class InventoryServiceUnitTest {
    lateinit var inventoryService: InventoryService
    private val itemRepository: ItemRepository = mock(ItemRepository::class.java)
    private val cartRepository: CartRepository = mock(CartRepository::class.java)

    @BeforeEach
    internal fun setUp() {
        val sampleItem = Item("item1", "TV tray", "Alf TV tray", 19.99)
        val sampleCartItem = CartItem(sampleItem)
        val sampleCart = Cart("My Cart", mutableListOf(sampleCartItem))

        `when`(cartRepository.findById(anyString())).thenReturn(Mono.empty())
        `when`(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem))
        `when`(cartRepository.save(any(Cart::class.java))).thenReturn(Mono.just(sampleCart))

        inventoryService = InventoryService(itemRepository, cartRepository)
    }

    @Test
    internal fun addItemToEmptyCartShouldProduceOneCartItem() {
        inventoryService.addItemToCart("My Cart", "item1")
            .`as`(StepVerifier::create)
            .expectNextMatches { cart ->
                assertThat(cart.cartItems).extracting(CartItem::quantity)
                    .containsExactlyInAnyOrder(Tuple(1))
                assertThat(cart.cartItems).extracting(CartItem::item)
                    .containsExactly(Tuple(Item("item1", "TV tray", "Alf TV tray", 19.99)))

                true
            }
            .verifyComplete()
    }
}
