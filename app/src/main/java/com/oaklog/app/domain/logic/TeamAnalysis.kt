package com.oaklog.app.domain.logic

/** One row of the team defensive grid: how each member fares against a single attacking type. */
data class TypeMatchupRow(
    val attackType: String,
    val multipliers: List<Double>,
) {
    val weakCount: Int get() = multipliers.count { it > 1.0 }
    val resistCount: Int get() = multipliers.count { it in 0.0001..0.9999 }
    val immuneCount: Int get() = multipliers.count { it == 0.0 }
}

data class TeamAnalysis(
    val typesRepresented: Map<String, Int>,
    val defensive: List<TypeMatchupRow>,
    /** Attack types at least two members are weak to and fewer members resist/are immune to than are weak. */
    val sharedWeaknesses: List<String>,
    /** Defender types hit super-effectively by at least one member's own type (STAB-based coverage only). */
    val offensiveCoverage: Set<String>,
    val uncoveredTypes: Set<String>,
)

/**
 * Defensive analysis uses members' types only (the accurate part). Offensive coverage is based on the
 * team's own types as a STAB proxy, because move-specific coverage depends on the chosen moves and
 * game mechanics; it's labelled as such in the UI and is not a battle simulation.
 */
object TeamAnalyzer {
    fun analyze(chart: TypeChart, memberTypes: List<List<String>>): TeamAnalysis {
        val represented = memberTypes.flatten().groupingBy { it }.eachCount()
            .toList().sortedByDescending { it.second }.toMap()
        val rows = chart.types.map { atk ->
            TypeMatchupRow(atk, memberTypes.map { chart.multiplier(atk, it) })
        }
        val shared = rows.filter { it.weakCount >= 2 && it.weakCount > it.resistCount + it.immuneCount }
            .sortedByDescending { it.weakCount }.map { it.attackType }
        val teamAttackTypes = memberTypes.flatten().toSet()
        val covered = chart.types.filter { def ->
            teamAttackTypes.any { chart.multiplier(it, listOf(def)) > 1.0 }
        }.toSet()
        val uncovered = if (memberTypes.isEmpty()) emptySet() else chart.types.toSet() - covered
        return TeamAnalysis(represented, rows, shared, covered, uncovered)
    }
}
