package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.PokemonSummary

enum class SortField { NUMBER, NAME, HEIGHT, WEIGHT, STAT_TOTAL }

enum class CaughtFilter { ANY, CAUGHT, UNCAUGHT }

/** Everything the Pokédex list can be narrowed by. Default instance means "show everything". */
data class PokedexFilters(
    val query: String = "",
    val generations: Set<Int> = emptySet(),
    val types: Set<String> = emptySet(),
    /** Species ids of the selected regional pokédex, or null for the National Dex. */
    val regionalSpeciesIds: Set<Int>? = null,
    val legendaryOnly: Boolean = false,
    val mythicalOnly: Boolean = false,
    val caught: CaughtFilter = CaughtFilter.ANY,
    val favoritesOnly: Boolean = false,
    val sortField: SortField = SortField.NUMBER,
    val sortDescending: Boolean = false,
    /** Regional entry numbers, used to order by number when a regional dex is active. */
    val regionalNumbers: Map<Int, Int> = emptyMap(),
) {
    /** Number of non-default narrowing filters (drives the badge on the filter button). */
    val activeCount: Int
        get() = listOf(
            generations.isNotEmpty(), types.isNotEmpty(), regionalSpeciesIds != null,
            legendaryOnly, mythicalOnly, caught != CaughtFilter.ANY, favoritesOnly,
        ).count { it }

    /** Whether any filter needs fields that only exist once a Pokémon has been downloaded. */
    val needsDetails: Boolean
        get() = legendaryOnly || mythicalOnly ||
            sortField in setOf(SortField.HEIGHT, SortField.WEIGHT, SortField.STAT_TOTAL)
}

object PokedexQuery {

    fun apply(
        all: List<PokemonSummary>,
        collection: Map<Int, CollectionEntry>,
        f: PokedexFilters,
    ): List<PokemonSummary> {
        val q = f.query.trim().lowercase().removePrefix("#")
        val numeric = q.toIntOrNull()

        val filtered = all.filter { p ->
            val entry = collection[p.id]
            matchesQuery(p, q, numeric) &&
                (f.generations.isEmpty() || p.generation in f.generations) &&
                (f.types.isEmpty() || f.types.all { it in p.types }) &&
                (f.regionalSpeciesIds == null || p.id in f.regionalSpeciesIds) &&
                (!f.legendaryOnly || p.isLegendary == true) &&
                (!f.mythicalOnly || p.isMythical == true) &&
                when (f.caught) {
                    CaughtFilter.ANY -> true
                    CaughtFilter.CAUGHT -> entry?.isCaught == true
                    CaughtFilter.UNCAUGHT -> entry?.isCaught != true
                } &&
                (!f.favoritesOnly || entry?.favorite == true)
        }
        return sort(filtered, f)
    }

    private fun matchesQuery(p: PokemonSummary, q: String, numeric: Int?): Boolean {
        if (q.isEmpty()) return true
        if (numeric != null && (p.id == numeric || p.id.toString().startsWith(q))) return true
        val normalized = p.name.replace("-", " ")
        return p.name.contains(q) || normalized.contains(q)
    }

    fun sort(list: List<PokemonSummary>, f: PokedexFilters): List<PokemonSummary> {
        val comparator: Comparator<PokemonSummary> = when (f.sortField) {
            SortField.NUMBER -> compareBy { f.regionalNumbers[it.id] ?: it.id }
            SortField.NAME -> compareBy { it.name }
            // Entries without downloaded details have no value; they always sink to the end.
            SortField.HEIGHT -> nullsLastBy(f.sortDescending) { it.height }
            SortField.WEIGHT -> nullsLastBy(f.sortDescending) { it.weight }
            SortField.STAT_TOTAL -> nullsLastBy(f.sortDescending) { it.baseStatTotal }
        }
        val directed = if (f.sortDescending && f.sortField in setOf(SortField.NUMBER, SortField.NAME)) {
            comparator.reversed()
        } else comparator
        return list.sortedWith(directed.thenBy { it.id })
    }

    private fun nullsLastBy(desc: Boolean, sel: (PokemonSummary) -> Int?): Comparator<PokemonSummary> =
        Comparator { a, b ->
            val x = sel(a); val y = sel(b)
            when {
                x == null && y == null -> 0
                x == null -> 1
                y == null -> -1
                desc -> y.compareTo(x)
                else -> x.compareTo(y)
            }
        }
}
