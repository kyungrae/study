package me.hama

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class InventoryService(
    private val itemRepository: ItemRepository,
    private val cartRepository: CartRepository
) {

    fun getCart(cartId: String): Mono<Cart> {
        return cartRepository.findById(cartId)
    }

    fun getInventory(): Flux<Item> {
        return itemRepository.findAll()
    }

    fun addItemToCart(cartId: String, id: String): Mono<Cart> {
        return cartRepository.findById(cartId)
            .defaultIfEmpty(Cart(cartId))
            .flatMap { cart ->
                cart.cartItems.stream()
                    .filter { cartItem -> cartItem.item.id == id }
                    .findAny()
                    .map { cartItem ->
                        cartItem.increment()
                        Mono.just(cart)
                    }
                    .orElseGet {
                        itemRepository.findById(id)
                            .map(::CartItem)
                            .map { cartItem ->
                                cart.cartItems.add(cartItem)
                                cart
                            }
                    }
            }
            .flatMap(cartRepository::save)
    }

    fun blockingAddItemToCart(cartId: String, id: String): Mono<Cart> {
        val myCart = cartRepository.findById(cartId)
            .defaultIfEmpty(Cart(cartId))
            .block()

        return myCart!!.cartItems.stream()
            .filter { cartItem -> cartItem.item.id == id }
            .findAny()
            .map { cartItem ->
                cartItem.increment()
                Mono.just(myCart)
            }
            .orElseGet {
                itemRepository.findById(id)
                    .map(::CartItem)
                    .map { cartItem ->
                        myCart.cartItems.add(cartItem)
                        myCart
                    }
            }
            .flatMap(cartRepository::save)
    }
}
