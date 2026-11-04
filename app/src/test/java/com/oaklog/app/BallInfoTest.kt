package com.oaklog.app

import com.oaklog.app.domain.logic.BallInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BallInfoTest {
    @Test
    fun `multiplier is the first factor in the effect`() {
        assertEquals("1.5×", BallInfo.multiplier("Tries to catch a wild Pokémon.  Success rate is 1.5×."))
        assertEquals("4×", BallInfo.multiplier("Success rate is 4× (Gen V: 5×), but only on the first turn."))
        assertNull(BallInfo.multiplier("Tries to catch a wild Pokémon."))
        assertNull(BallInfo.multiplier(null))
    }

    @Test
    fun `detail drops the sentence every ball repeats`() {
        assertEquals("Success rate is 3× for water and bug Pokémon.", BallInfo.detail("Tries to catch a wild Pokémon.  Success rate is 3× for water and bug Pokémon."))
        assertNull(BallInfo.detail("Tries to catch a wild Pokémon."))
    }

    @Test
    fun `category labels are readable`() {
        assertEquals("Apricorn", BallInfo.categoryLabel("apricorn-balls"))
    }
}
