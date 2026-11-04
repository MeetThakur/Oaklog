package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.BerryInfo

object BerryFilter {
    /** Flavors that occur, in the order a person thinks of them. */
    val flavorOrder = listOf("spicy", "dry", "sweet", "bitter", "sour")

    fun flavors(berries: List<BerryInfo>): List<String> {
        val present = berries.flatMap { it.flavors }.map { it.name }.toSet()
        return flavorOrder.filter { it in present }
    }

    /** [flavor] keeps berries that have it, strongest first; [query] matches the name. */
    fun filter(berries: List<BerryInfo>, flavor: String?, query: String): List<BerryInfo> {
        val q = query.trim().lowercase()
        val matches = berries.filter { b ->
            (q.isEmpty() || b.name.contains(q)) && (flavor == null || b.flavors.any { it.name == flavor })
        }
        return if (flavor == null) matches
        else matches.sortedByDescending { b -> b.flavors.first { it.name == flavor }.potency }
    }

    /** Total hours from planting to ripe: PokéAPI's growth time is per stage and there are four. */
    fun hoursToRipen(berry: BerryInfo): Int = berry.growthTime * 4
}
