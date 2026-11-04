package com.oaklog.app

import com.oaklog.app.domain.logic.Learnset
import com.oaklog.app.domain.model.LearnEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LegalMovesTest {

    private val entries = listOf(
        LearnEntry("ember", "level-up", 7, "red-blue"),
        LearnEntry("ember", "level-up", 4, "scarlet-violet"),
        LearnEntry("flamethrower", "machine", 0, "red-blue"),
        LearnEntry("flamethrower", "level-up", 36, "scarlet-violet"),
        LearnEntry("dragon-dance", "egg", 0, "scarlet-violet"),
        LearnEntry("mega-punch", "machine", 0, "red-blue"),
    )

    @Test
    fun `one row per move with its lowest level and other sources`() {
        val moves = Learnset.legalMoves(entries).associateBy { it.name }
        assertEquals(listOf("Lv 4"), moves.getValue("ember").sources)
        assertEquals(listOf("Lv 36", "TM"), moves.getValue("flamethrower").sources)
        assertEquals(listOf("Egg"), moves.getValue("dragon-dance").sources)
    }

    @Test
    fun `limiting to a game drops moves that game cannot teach`() {
        val names = Learnset.legalMoves(entries, "red-blue").map { it.name }
        assertEquals(listOf("ember", "flamethrower", "mega-punch"), names)
        assertFalse("dragon-dance" in names)
    }

    @Test
    fun `slug turns typed names into api names`() {
        assertEquals("quick-attack", Learnset.slug("  Quick Attack "))
        assertEquals("static", Learnset.slug("Static"))
    }
}
