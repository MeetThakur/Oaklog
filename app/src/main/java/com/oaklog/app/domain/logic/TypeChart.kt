package com.oaklog.app.domain.logic

/**
 * Type effectiveness built from PokéAPI damage relations (nothing hardcoded).
 *
 * [attackMultipliers] maps attacker type -> (defender type -> multiplier) for every pair where the
 * multiplier is not 1×. Missing pairs are neutral.
 */
class TypeChart(
    val types: List<String>,
    private val attackMultipliers: Map<String, Map<String, Double>>,
) {
    /** Multiplier of a [attacker]-type move against a defender with the given [defenderTypes]. */
    fun multiplier(attacker: String, defenderTypes: List<String>): Double =
        defenderTypes.fold(1.0) { acc, d -> acc * (attackMultipliers[attacker]?.get(d) ?: 1.0) }

    /** Multiplier for every attacking type against this defender. */
    fun defensiveProfile(defenderTypes: List<String>): Map<String, Double> =
        types.associateWith { multiplier(it, defenderTypes) }

    /** Defender types hit super-effectively/not-very/not-at-all by a single attacking type. */
    fun offensiveProfile(attacker: String): Map<String, Double> =
        types.associateWith { multiplier(attacker, listOf(it)) }

    fun weaknesses(defenderTypes: List<String>) = defensiveProfile(defenderTypes).filterValues { it > 1.0 }
    fun resistances(defenderTypes: List<String>) = defensiveProfile(defenderTypes).filterValues { it in 0.0001..0.9999 }
    fun immunities(defenderTypes: List<String>) = defensiveProfile(defenderTypes).filterValues { it == 0.0 }

    companion object {
        /**
         * Build from per-type relation lists. Uses the "damage_to" lists as the source of truth for
         * attacker->defender and ignores "damage_from" (the API publishes both views of the same data).
         */
        fun fromRelations(relations: Map<String, Relations>): TypeChart {
            val types = relations.keys.toList()
            val map = relations.mapValues { (_, r) ->
                buildMap<String, Double> {
                    r.doubleTo.forEach { put(it, 2.0) }
                    r.halfTo.forEach { put(it, 0.5) }
                    r.noneTo.forEach { put(it, 0.0) }
                }
            }
            return TypeChart(types, map)
        }
    }

    data class Relations(val doubleTo: List<String>, val halfTo: List<String>, val noneTo: List<String>)
}
