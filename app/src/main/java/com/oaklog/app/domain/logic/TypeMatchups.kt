package com.oaklog.app.domain.logic

/** How one Pokémon fares against each type, by its typing alone (abilities are not considered). */
data class Matchups(
    val weak4: List<String>,
    val weak2: List<String>,
    val resist2: List<String>,
    val resist4: List<String>,
    val immune: List<String>,
    /** Defender types that at least one of the Pokémon's own types hits super-effectively. */
    val strongAgainst: List<String>,
) {
    val isEmpty: Boolean
        get() = weak4.isEmpty() && weak2.isEmpty() && resist2.isEmpty() && resist4.isEmpty() &&
            immune.isEmpty() && strongAgainst.isEmpty()
}

object TypeMatchups {
    fun of(chart: TypeChart, types: List<String>): Matchups {
        val defence = chart.defensiveProfile(types)
        fun typesWhere(test: (Double) -> Boolean) = defence.filterValues(test).keys.sorted()
        val strong = chart.types.filter { defender ->
            types.any { chart.multiplier(it, listOf(defender)) > 1.0 }
        }.sorted()
        return Matchups(
            weak4 = typesWhere { it >= 4.0 },
            weak2 = typesWhere { it > 1.0 && it < 4.0 },
            resist2 = typesWhere { it > 0.0 && it < 1.0 && it > 0.26 },
            resist4 = typesWhere { it > 0.0 && it <= 0.26 },
            immune = typesWhere { it == 0.0 },
            strongAgainst = strong,
        )
    }
}
