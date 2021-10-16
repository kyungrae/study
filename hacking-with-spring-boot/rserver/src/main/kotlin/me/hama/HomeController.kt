package me.hama

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.reactive.result.view.Rendering
import reactor.core.publisher.Mono

@Controller
class HomeController(
    private val inventoryService: InventoryService
) {

    @GetMapping
    fun home(auth: Authentication): Mono<Rendering> {
        return Mono.just(
            Rendering
                .view("home.html")
                .modelAttribute("items", inventoryService.getInventory())
                .modelAttribute("cart", inventoryService.getCart(auth.name).defaultIfEmpty(Cart(auth.name)))
                .modelAttribute("auth", auth)
                .build()
        )
    }

    @PostMapping("/add/{id}")
    fun addToCart(@PathVariable id: String): Mono<String>? {
        return inventoryService.addItemToCart("My Cart", id)
            .thenReturn("redirect:/") // <7>
    }
}
