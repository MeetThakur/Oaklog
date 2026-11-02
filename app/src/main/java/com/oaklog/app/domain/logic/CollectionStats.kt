package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.domain.model.PokemonSummary

data class Progress(val done: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
    val percent: Int get() = (fraction * 100).toInt()
}

data class CollectionSummary(
    val caught: Progress,
    val seen: Int,
    val shiny: Int,
    val favorites: Int,
    val byGeneration: Map<Int, Progress>,
)

object CollectionStats {
    fun summarize(all: List<PokemonSummary>, collection: Map<Int, CollectionEntry>): CollectionSummary {
        val entries = all.mapNotNull { collection[it.id] }
        val byGen = all.filter { it.generation != null }.groupBy { it.generation!! }.toSortedMap()
            .mapValues { (_, members) -> progress(members.map { it.id }.toSet(), collection) }
        return CollectionSummary(
            caught = progress(all.map { it.id }.toSet(), collection),
            seen = entries.count { it.status == CollectionStatus.SEEN },
            shiny = entries.count { it.shiny },
            favorites = entries.count { it.favorite },
            byGeneration = byGen,
        )
    }

    /** Progress over an arbitrary id set (e.g. a regional pokédex). */
    fun progress(speciesIds: Set<Int>, collection: Map<Int, CollectionEntry>): Progress =
        Progress(speciesIds.count { collection[it]?.isCaught == true }, speciesIds.size)

    /** Rules: caught implies seen; un-catching a shiny keeps it flagged only while caught. */
    fun cycleStatus(current: CollectionEntry): CollectionEntry = when (current.status) {
        CollectionStatus.NOT_CAUGHT -> current.copy(status = CollectionStatus.SEEN)
        CollectionStatus.SEEN -> current.copy(status = CollectionStatus.CAUGHT)
        CollectionStatus.CAUGHT -> current.copy(status = CollectionStatus.NOT_CAUGHT, shiny = false)
    }

    /** Marking shiny implies caught. */
    fun toggleShiny(current: CollectionEntry): CollectionEntry =
        if (current.shiny) current.copy(shiny = false)
        else current.copy(shiny = true, status = CollectionStatus.CAUGHT)
}
