package com.oaklog.app

import com.oaklog.app.domain.logic.TypeChart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TypeChartTest {

    private lateinit var typeChart: TypeChart

    @Before
    fun setup() {
        val relations = mapOf(
            "water" to TypeChart.Relations(
                doubleTo = listOf("fire", "ground", "rock"),
                halfTo = listOf("water", "grass", "dragon"),
                noneTo = emptyList(),
            ),
            "fire" to TypeChart.Relations(
                doubleTo = listOf("grass", "ice", "bug", "steel"),
                halfTo = listOf("fire", "water", "rock", "dragon"),
                noneTo = emptyList(),
            ),
            "electric" to TypeChart.Relations(
                doubleTo = listOf("water", "flying"),
                halfTo = listOf("electric", "grass", "dragon"),
                noneTo = listOf("ground"),
            ),
            "grass" to TypeChart.Relations(
                doubleTo = listOf("water", "ground", "rock"),
                halfTo = listOf("fire", "grass", "poison", "flying", "bug", "dragon", "steel"),
                noneTo = emptyList(),
            ),
            "ground" to TypeChart.Relations(
                doubleTo = listOf("fire", "electric", "poison", "rock", "steel"),
                halfTo = listOf("grass", "bug"),
                noneTo = listOf("flying"),
            ),
        )
        typeChart = TypeChart.fromRelations(relations)
    }

    @Test
    fun singleTypeEffectiveness() {
        assertEquals(2.0, typeChart.multiplier("water", listOf("fire")), 0.001)
        assertEquals(0.5, typeChart.multiplier("fire", listOf("water")), 0.001)
        assertEquals(0.0, typeChart.multiplier("electric", listOf("ground")), 0.001)
        assertEquals(1.0, typeChart.multiplier("water", listOf("electric")), 0.001)
    }

    @Test
    fun dualTypeEffectiveness() {
        // Water/Ground defender vs Grass attacker -> 2.0 * 2.0 = 4.0 (quad weakness)
        assertEquals(4.0, typeChart.multiplier("grass", listOf("water", "ground")), 0.001)

        // Water/Ground defender vs Electric attacker -> 2.0 * 0.0 = 0.0 (immunity overrides weakness)
        assertEquals(0.0, typeChart.multiplier("electric", listOf("water", "ground")), 0.001)
    }

    @Test
    fun defensiveWeaknessesAndImmunities() {
        val defenderTypes = listOf("water", "ground")
        val weaknesses = typeChart.weaknesses(defenderTypes)
        val immunities = typeChart.immunities(defenderTypes)

        assertTrue(weaknesses.containsKey("grass"))
        assertEquals(4.0, weaknesses["grass"]!!, 0.001)

        assertTrue(immunities.containsKey("electric"))
        assertEquals(0.0, immunities["electric"]!!, 0.001)
    }
}
