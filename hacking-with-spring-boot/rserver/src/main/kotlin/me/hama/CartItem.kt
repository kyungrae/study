package me.hama

data class CartItem(
    var item: Item,
    var quantity: Int = 1
) {

    fun increment() {
        quantity++
    }
}
