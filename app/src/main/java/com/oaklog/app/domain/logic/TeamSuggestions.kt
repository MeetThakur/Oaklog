package com.oaklog.app.domain.logic

data class TeamSuggestion(
    val title: String,
    val detail: String,
    /** Types that would patch the gap, best first. */
    val suggestedTypes: List<String> = emptyList(),
)

data class SpeedTierEntry(
    val name: String,
    val baseSpeed: Int,
    /** Speed at level 50, neutral nature, 31 IV, no EVs. */
    val speedL50: Int,
    val speedL100: Int,
)

/**
 * Turns the raw [TeamAnalysis] into plain-language advice. It reasons over types only, like the
 * analysis itself, so the wording says "answer" (a resist or immunity), never "counter".
 */
object TeamSuggestions {
    fun generate(chart: TypeChart, analysis: TeamAnalysis, memberCount: Int): List<TeamSuggestion> {
        if (memberCount == 0) return emptyList()
        val out = mutableListOf<TeamSuggestion>()

        val unanswered = analysis.defensive
            .filter { it.weakCount > 0 && it.resistCount + it.immuneCount == 0 }
            .sortedByDescending { it.weakCount }
        for (row in unanswered.take(3)) {
            val fixes = typesThatAnswer(chart, row.attackType, analysis.uncoveredTypes)
            out += TeamSuggestion(
                title = "Lacks a ${row.attackType.cap()} answer",
                detail = "${row.weakCount} of $memberCount ${if (memberCount == 1) "member is" else "members are"} " +
                    "weak to ${row.attackType.cap()} and none resist or are immune to it.",
                suggestedTypes = fixes,
            )
        }

        if (analysis.uncoveredTypes.isNotEmpty()) {
            val gaps = analysis.uncoveredTypes.toList()
            val fixes = chart.types.filter { atk ->
                gaps.count { chart.multiplier(atk, listOf(it)) > 1.0 } > 0
            }.sortedByDescending { atk -> gaps.count { chart.multiplier(atk, listOf(it)) > 1.0 } }.take(4)
            out += TeamSuggestion(
                title = "No STAB pressure on ${gaps.size} type${if (gaps.size == 1) "" else "s"}",
                detail = "Nothing on the team hits ${listPreview(gaps)} super-effectively " +
                    "with its own types. Coverage moves can fill this in.",
                suggestedTypes = fixes,
            )
        }

        val duplicated = analysis.typesRepresented.filterValues { it >= 3 }.keys
        if (duplicated.isNotEmpty()) {
            out += TeamSuggestion(
                title = "Heavy on ${duplicated.joinToString { it.cap() }}",
                detail = "Three or more members share a type, which stacks their weaknesses.",
            )
        }

        if (memberCount in 1..5) {
            out += TeamSuggestion(
                title = "${6 - memberCount} open slot${if (6 - memberCount == 1) "" else "s"}",
                detail = "Fill them with types the points above call for.",
            )
        }
        return out
    }

    /** Defender types that resist or are immune to [attackType], preferring ones that also hit [gaps]. */
    private fun typesThatAnswer(chart: TypeChart, attackType: String, gaps: Set<String>): List<String> {
        val resisting = chart.types.filter { chart.multiplier(attackType, listOf(it)) < 1.0 }
        // Immunities first, then resists, and within each prefer types that also pressure a gap.
        return resisting.sortedWith(
            compareBy<String>({ chart.multiplier(attackType, listOf(it)) })
                .thenByDescending { t -> gaps.count { chart.multiplier(t, listOf(it)) > 1.0 } },
        ).take(4)
    }

    fun speedTiers(members: List<Pair<String, Int>>): List<SpeedTierEntry> =
        members.map { (name, base) ->
            SpeedTierEntry(name, base, StatCalc.speed(base, 50), StatCalc.speed(base, 100))
        }.sortedByDescending { it.baseSpeed }

    /** First few names, then a count, so one tip never turns into a paragraph. */
    private fun listPreview(types: List<String>, shown: Int = 5): String =
        if (types.size <= shown) types.joinToString { it.cap() }
        else types.take(shown).joinToString { it.cap() } + " and ${types.size - shown} more"

    private fun String.cap() = replaceFirstChar { it.uppercase() }
}
