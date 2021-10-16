package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ItemUnitTest {

    @Test
    fun itemBasicsShouldWork() {
        val sampleItem = Item("item1", "TV tray", "Alf TV tray", 19.99)

        assertThat(sampleItem.id).isEqualTo("item1")
        assertThat(sampleItem.name).isEqualTo("TV tray")
        assertThat(sampleItem.description).isEqualTo("Alf TV tray")
        assertThat(sampleItem.price).isEqualTo(19.99)

        val sampleItem2 = Item("item1", "TV tray", "Alf TV tray", 19.99)
        assertThat(sampleItem).isEqualTo(sampleItem2)
    }
}
