package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.LearnEntry

/** Helpers for presenting a Pokémon's learnset by game and learn method. */
object Learnset {

    /** Version groups in release order; unknown groups sort before these. */
    private val versionGroupOrder = listOf(
        "red-blue", "yellow", "gold-silver", "crystal", "ruby-sapphire", "emerald", "firered-leafgreen",
        "diamond-pearl", "platinum", "heartgold-soulsilver", "black-white", "black-2-white-2", "x-y",
        "omega-ruby-alpha-sapphire", "sun-moon", "ultra-sun-ultra-moon", "lets-go-pikachu-lets-go-eevee",
        "sword-shield", "the-isle-of-armor", "the-crown-tundra", "brilliant-diamond-and-shining-pearl",
        "legends-arceus", "scarlet-violet", "the-teal-mask", "the-indigo-disk",
    )

    private val methodOrder = listOf("level-up", "machine", "egg", "tutor")

    /** Newest game first. */
    fun versionGroups(entries: List<LearnEntry>): List<String> =
        entries.map { it.versionGroup }.distinct().sortedByDescending { versionGroupOrder.indexOf(it) }

    fun methods(entries: List<LearnEntry>, versionGroup: String?): List<String> =
        entries.filter { it.versionGroup == versionGroup }
            .map { it.method }
            .distinct()
            .sortedBy { methodOrder.indexOf(it).let { i -> if (i < 0) methodOrder.size else i } }

    /** Level-up moves ordered by level, everything else alphabetically. */
    fun select(entries: List<LearnEntry>, versionGroup: String?, method: String?): List<LearnEntry> {
        val rows = entries.filter { it.versionGroup == versionGroup && it.method == method }
        return if (method == "level-up") rows.sortedWith(compareBy({ it.level }, { it.moveName }))
        else rows.sortedBy { it.moveName }
    }

    fun prettyMethod(method: String): String = when (method) {
        "level-up" -> "Level up"
        "machine" -> "TM / HM"
        "egg" -> "Egg moves"
        "tutor" -> "Move tutor"
        else -> NameFormatter.pretty(method)
    }
}
