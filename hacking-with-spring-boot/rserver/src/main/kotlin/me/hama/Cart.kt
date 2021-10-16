package me.hama

import org.springframework.data.annotation.Id

data class Cart(
    @Id var id: String,
    var cartItems: MutableList<CartItem> = mutableListOf()
)
