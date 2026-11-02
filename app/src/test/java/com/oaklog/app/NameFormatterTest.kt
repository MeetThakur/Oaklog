package com.oaklog.app

import com.oaklog.app.domain.logic.NameFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class NameFormatterTest {

    @Test
    fun formatsSpecialPokemonNames() {
        assertEquals("Mr. Mime", NameFormatter.pretty("mr-mime"))
        assertEquals("Mime Jr.", NameFormatter.pretty("mime-jr"))
        assertEquals("Type: Null", NameFormatter.pretty("type-null"))
        assertEquals("Ho-Oh", NameFormatter.pretty("ho-oh"))
        assertEquals("Porygon-Z", NameFormatter.pretty("porygon-z"))
        assertEquals("Pikachu", NameFormatter.pretty("pikachu"))
        assertEquals("Iron Valiant", NameFormatter.pretty("iron-valiant"))
    }

    @Test
    fun formatsDexNumbers() {
        assertEquals("#0001", NameFormatter.dexNumber(1))
        assertEquals("#0025", NameFormatter.dexNumber(25))
        assertEquals("#0151", NameFormatter.dexNumber(151))
        assertEquals("#1025", NameFormatter.dexNumber(1025))
    }

    @Test
    fun formatsMetrics() {
        assertEquals("0.4 m", NameFormatter.heightMeters(4))
        assertEquals("6.0 kg", NameFormatter.weightKilograms(60))
    }

    @Test
    fun formatsGenerations() {
        assertEquals("I", NameFormatter.generationRoman(1))
        assertEquals("IV", NameFormatter.generationRoman(4))
        assertEquals("IX", NameFormatter.generationRoman(9))
        assertEquals(4, NameFormatter.generationFromSlug("generation-iv"))
        assertEquals(9, NameFormatter.generationFromSlug("generation-ix"))
    }
}
