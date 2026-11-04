package com.oaklog.app

import com.oaklog.app.domain.logic.DamageCalc
import com.oaklog.app.domain.logic.Natures
import com.oaklog.app.domain.logic.StatCalc
import com.oaklog.app.domain.logic.TeamAnalyzer
import com.oaklog.app.domain.logic.TeamSuggestions
import com.oaklog.app.domain.logic.TypeChart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatCalcTest {

    private val chart = TypeChart.fromRelations(
        mapOf(
            "fire" to TypeChart.Relations(doubleTo = listOf("grass"), halfTo = listOf("water"), noneTo = emptyList()),
            "water" to TypeChart.Relations(doubleTo = listOf("fire"), halfTo = listOf("grass"), noneTo = emptyList()),
            "grass" to TypeChart.Relations(doubleTo = listOf("water"), halfTo = listOf("fire"), noneTo = emptyList()),
            "ground" to TypeChart.Relations(doubleTo = listOf("fire"), halfTo = listOf("grass"), noneTo = emptyList()),
            "flying" to TypeChart.Relations(doubleTo = listOf("grass"), halfTo = emptyList(), noneTo = emptyList()),
        )
    )

    @Test
    fun `hp and attack match the published level 78 Garchomp example`() {
        assertEquals(289, StatCalc.hp(base = 108, iv = 24, ev = 74, level = 78))
        assertEquals(278, StatCalc.other(130, 12, 190, 78, Natures.byName("Adamant"), "attack"))
    }

    @Test
    fun `nature lowers and raises only its two stats`() {
        val adamant = Natures.byName("Adamant")
        val neutral = StatCalc.other(100, 31, 0, 50, Natures.neutral, "attack")
        assertTrue(StatCalc.other(100, 31, 0, 50, adamant, "attack") > neutral)
        assertTrue(StatCalc.other(100, 31, 0, 50, adamant, "special-attack") < neutral)
        assertEquals(neutral, StatCalc.other(100, 31, 0, 50, adamant, "defense"))
    }

    @Test
    fun `base one hp is always one`() {
        assertEquals(1, StatCalc.hp(base = 1, iv = 31, ev = 252, level = 100))
    }

    @Test
    fun `super effective stab hit is bigger than a neutral one`() {
        val se = DamageCalc.calculate(50, 80, 100, 100, 150, "fire", listOf("fire"), listOf("grass"), chart)
        val neutral = DamageCalc.calculate(50, 80, 100, 100, 150, "fire", listOf("fire"), listOf("flying"), chart)
        assertTrue(se.stab)
        assertEquals(2.0, se.effectiveness, 0.0)
        assertTrue(se.max > neutral.max)
        assertTrue(se.min <= se.max)
    }

    @Test
    fun `immune target takes no damage and cannot be koed`() {
        val none = TypeChart.fromRelations(
            mapOf("ground" to TypeChart.Relations(emptyList(), emptyList(), listOf("flying")), "flying" to TypeChart.Relations(emptyList(), emptyList(), emptyList()))
        )
        val r = DamageCalc.calculate(50, 100, 100, 100, 150, "ground", listOf("ground"), listOf("flying"), none)
        assertEquals(0, r.max)
        assertNull(r.hitsToKo)
    }

    @Test
    fun `team with no resist to a type it is weak to gets an answer suggestion`() {
        val analysis = TeamAnalyzer.analyze(chart, listOf(listOf("fire"), listOf("fire"), listOf("fire")))
        val tips = TeamSuggestions.generate(chart, analysis, 3)
        assertTrue(tips.any { it.title.contains("Water") })
        assertTrue(tips.first { it.title.contains("Water") }.suggestedTypes.isNotEmpty())
    }

    @Test
    fun `speed tiers sort fastest first`() {
        val tiers = TeamSuggestions.speedTiers(listOf("a" to 50, "b" to 130, "c" to 90))
        assertEquals(listOf("b", "c", "a"), tiers.map { it.name })
        assertTrue(tiers.first().speedL100 > tiers.first().speedL50)
    }
}
