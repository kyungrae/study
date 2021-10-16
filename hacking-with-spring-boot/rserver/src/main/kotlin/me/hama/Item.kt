package me.hama

import org.springframework.data.annotation.Id

class Item(
    @Id
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var price: Double = 0.0
) {
    override fun equals(other: Any?): Boolean {
        if(other is Item)
            return id == other.id
        return false
    }
}
