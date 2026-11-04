package com.oaklog.app

import com.oaklog.app.domain.logic.TeamAnalyzer
import com.oaklog.app.domain.logic.TypeChart
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TeamAnalyzerTest {

    private lateinit var typeChart: TypeChart

    @Before
    fun setup() {
        val relations = mapOf(
            "water" to TypeChart.Relations(
                doubleTo = listOf("fire", "rock"),
                halfTo = listOf("water", "grass"),
                noneTo = emptyList(),
            ),
            "fire" to TypeChart.Relations(
                doubleTo = listOf("grass", "ice"),
                halfTo = listOf("fire", "water"),
                noneTo = emptyList(),
            ),
            "grass" to TypeChart.Relations(
                doubleTo = listOf("water", "rock"),
                halfTo = listOf("fire", "grass"),
                noneTo = emptyList(),
            ),
            "rock" to TypeChart.Relations(
                doubleTo = listOf("fire", "flying"),
                halfTo = listOf("fighting"),
                noneTo = emptyList(),
            ),
        )
        typeChart = TypeChart.fromRelations(relations)
    }

    @Test
    fun teamAnalysisTypesRepresented() {
        val memberTypes = listOf(
            listOf("water"),
            listOf("water", "rock"),
            listOf("fire"),
        )
        val analysis = TeamAnalyzer.analyze(typeChart, memberTypes)

        assertEquals(2, analysis.typesRepresented["water"])
        assertEquals(1, analysis.typesRepresented["rock"])
        assertEquals(1, analysis.typesRepresented["fire"])
    }

    @Test
    fun sharedWeaknessesDetection() {
        // Both member 1 (water) and member 2 (rock) are weak to grass!
        val memberTypes = listOf(
            listOf("water"),
            listOf("rock"),
        )
        val analysis = TeamAnalyzer.analyze(typeChart, memberTypes)

        assertTrue(analysis.sharedWeaknesses.contains("grass"))
    }

    @Test
    fun offensiveCoverageCalculation() {
        val memberTypes = listOf(
            listOf("water"),
            listOf("fire"),
        )
        val analysis = TeamAnalyzer.analyze(typeChart, memberTypes)

        // Water covers fire, rock. Fire covers grass, ice.
        assertTrue(analysis.offensiveCoverage.contains("fire"))
        assertTrue(analysis.offensiveCoverage.contains("rock"))
        assertTrue(analysis.offensiveCoverage.contains("grass"))
    }
}

