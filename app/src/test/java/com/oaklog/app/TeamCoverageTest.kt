package com.oaklog.app

import com.oaklog.app.domain.logic.Attack
import com.oaklog.app.domain.logic.TeamCoverage
import com.oaklog.app.domain.logic.TypeChart
import org.junit.Assert.assertEquals
import org.junit.Test

class TeamCoverageTest {

    private val chart = TypeChart.fromRelations(
        mapOf(
            "fire" to TypeChart.Relations(listOf("grass", "ice"), listOf("water"), emptyList()),
            "water" to TypeChart.Relations(listOf("fire"), listOf("grass"), emptyList()),
            "grass" to TypeChart.Relations(listOf("water"), listOf("fire"), emptyList()),
            "ice" to TypeChart.Relations(listOf("grass"), listOf("fire", "water"), emptyList()),
        ),
    )

    @Test
    fun `a move can cover a type the member's own types cannot`() {
        val stab = listOf(Attack("Charizard", null, "fire"))
        val moves = listOf(Attack("Charizard", "surf", "water"))
        val r = TeamCoverage.compute(chart, stab, moves)
        assertEquals(setOf("grass", "ice", "fire"), r.covered)
        assertEquals(setOf("fire"), r.onlyViaMoves)
    }

    @Test
    fun `with no moves chosen coverage equals stab coverage`() {
        val r = TeamCoverage.compute(chart, listOf(Attack("Charizard", null, "fire")), emptyList())
        assertEquals(setOf("grass", "ice"), r.covered)
        assertEquals(emptySet<String>(), r.onlyViaMoves)
    }

    @Test
    fun `hitters name who covers each type`() {
        val r = TeamCoverage.compute(
            chart,
            listOf(Attack("A", null, "fire")),
            listOf(Attack("B", "ice-beam", "ice")),
        )
        assertEquals(listOf("A", "B"), r.hitters.getValue("grass").map { it.member })
        assertEquals(listOf("A"), r.hitters.getValue("ice").map { it.member })
    }
}
