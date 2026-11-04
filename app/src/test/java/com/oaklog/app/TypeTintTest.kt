package com.oaklog.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.oaklog.app.presentation.theme.typeTintFor
import org.junit.Assert.assertTrue
import org.junit.Test

class TypeTintTest {

    private val types = listOf(
        "normal", "fire", "water", "grass", "electric", "ice", "fighting", "poison", "ground", "flying",
        "psychic", "bug", "rock", "ghost", "dragon", "steel", "fairy", "dark", "stellar", "unknown",
    )

    private fun contrast(a: Color, b: Color): Double {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    @Test
    fun `text on every type container clears AAA in light and dark`() {
        for (dark in listOf(false, true)) {
            for (type in types) {
                val tint = typeTintFor(type, dark)
                val ratio = contrast(tint.onContainer, tint.container)
                assertTrue("$type dark=$dark contrast $ratio", ratio >= 7.0)
            }
        }
    }

    @Test
    fun `art disc stays distinguishable from its container`() {
        for (dark in listOf(false, true)) {
            for (type in types) {
                val tint = typeTintFor(type, dark)
                assertTrue("$type dark=$dark", contrast(tint.art, tint.container) >= 1.05)
            }
        }
    }
}
