package com.oaklog.app.domain.logic

/** One way the team can attack: a member's own type (STAB, no move) or a damaging move it knows. */
data class Attack(val member: String, val move: String?, val type: String)

data class CoverageResult(
    /** Defender types some attack hits super-effectively. */
    val covered: Set<String>,
    /** Covered, but only because of a chosen move: the member's own types wouldn't do it. */
    val onlyViaMoves: Set<String>,
    /** For each covered defender type, every attack that hits it, so the UI can say who. */
    val hitters: Map<String, List<Attack>>,
)

object TeamCoverage {

    /**
     * [stab] are members' own types, [moves] their damaging moves (status moves cover nothing, so
     * the caller leaves them out). Keeping the two lists apart is what lets us say a type is only
     * covered thanks to a move.
     */
    fun compute(chart: TypeChart, stab: List<Attack>, moves: List<Attack>): CoverageResult {
        val all = stab + moves
        val hitters = chart.types.associateWith { def ->
            all.filter { chart.multiplier(it.type, listOf(def)) > 1.0 }
        }.filterValues { it.isNotEmpty() }
        val viaStab = chart.types.filter { def -> stab.any { chart.multiplier(it.type, listOf(def)) > 1.0 } }.toSet()
        val covered = hitters.keys
        return CoverageResult(covered, covered - viaStab, hitters)
    }
}
