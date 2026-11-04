package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.AreaSpawn
import com.oaklog.app.domain.model.EncounterSlot
import com.oaklog.app.domain.model.GameSummary
import com.oaklog.app.domain.model.LocationSummary
import com.oaklog.app.domain.model.SummaryMon

/** Display helpers for PokéAPI place slugs such as "kanto-route-2-south-towards-viridian-city". */
object Places {

    /** "kanto-route-2" in region "kanto" reads as "Route 2". */
    fun locationLabel(location: String, region: String?): String {
        val trimmed = if (region != null && location.startsWith("$region-")) location.removePrefix("$region-") else location
        return NameFormatter.pretty(trimmed)
    }

    /**
     * Areas repeat their location's name ("kanto-route-2-south-towards-viridian-city"), so show only
     * the part that tells them apart. A lone "-area" suffix is the whole place, not a sub-area.
     */
    fun areaLabel(area: String, location: String): String {
        val rest = area.removePrefix(location).trim('-')
        return when {
            rest.isEmpty() || rest == "area" -> "Main area"
            else -> NameFormatter.pretty(rest.removeSuffix("-area").trim('-'))
        }
    }

    /** Folds the spawns of all of a location's areas into one summary. */
    fun summarize(areas: List<List<AreaSpawn>>): LocationSummary {
        val spawns = areas.flatten()
        val all = fold(spawns, game = null)
        val games = spawns.flatMap { it.entries }.map { it.version }.distinct()
        return LocationSummary(
            pokemon = all.pokemon,
            minLevel = all.minLevel,
            maxLevel = all.maxLevel,
            perGame = games.associateWith { fold(spawns, it) },
        )
    }

    /** One game's view: only spawns that exist in [game], with levels taken from that game's entries. */
    private fun fold(spawns: List<AreaSpawn>, game: String?): GameSummary {
        val here = if (game == null) spawns else spawns.filter { s -> s.entries.any { it.version == game } }
        val entries = here.flatMap { s -> s.entries.filter { game == null || it.version == game } }
        val pokemon = here
            .groupBy { it.pokemonName }
            .map { (name, group) -> SummaryMon(name, group.firstNotNullOfOrNull { it.speciesId }) }
            .sortedWith(compareBy({ it.id ?: Int.MAX_VALUE }, { it.name }))
        return GameSummary(
            pokemon = pokemon,
            minLevel = entries.map { it.minLevel }.filter { it > 0 }.minOrNull() ?: 0,
            maxLevel = entries.maxOfOrNull { it.maxLevel } ?: 0,
        )
    }

    /** "surf" → "Surfing". Methods without a friendlier name fall back to the prettified slug. */
    fun methodLabel(method: String): String = when (method) {
        "walk" -> "Walking"
        "surf" -> "Surfing"
        "old-rod" -> "Old rod"
        "good-rod" -> "Good rod"
        "super-rod" -> "Super rod"
        "only-one" -> "Static encounter"
        "gift" -> "Gift"
        else -> NameFormatter.pretty(method).replaceFirstChar { it.uppercase() }
    }

    /**
     * Conditions come as "group-value" slugs: "time-night", "swarm-yes", "radar-on", "slot2-sapphire".
     * Turn the common ones into plain words and leave unknown ones readable rather than wrong.
     */
    fun conditionLabel(slug: String): String {
        val group = slug.substringBefore('-')
        val value = slug.substringAfter('-', "")
        return when {
            group == "time" -> NameFormatter.pretty(value)
            group == "season" -> NameFormatter.pretty(value)
            group == "swarm" -> if (value == "yes") "Swarm" else "No swarm"
            group == "radar" -> if (value == "on") "PokéRadar" else "No PokéRadar"
            group == "radio" -> if (value == "off") "Radio off" else "${NameFormatter.pretty(value)} radio"
            group == "story" -> NameFormatter.pretty(value)
            group.startsWith("slot") && value.isNotEmpty() -> "${NameFormatter.pretty(value)} in slot ${group.removePrefix("slot")}"
            else -> NameFormatter.pretty(slug)
        }
    }

    /** "Surfing · 20% · Lv 20–30 · Night" */
    fun slotSummary(slot: EncounterSlot): String = buildList {
        add(methodLabel(slot.method))
        if (slot.chance > 0) add("${slot.chance}%")
        if (slot.maxLevel > 0) add(levelRange(slot.minLevel, slot.maxLevel))
        slot.conditions.forEach { add(conditionLabel(it)) }
    }.joinToString(" · ")

    fun levelRange(min: Int, max: Int): String = when {
        max <= 0 -> "—"
        min == max -> "Lv $min"
        else -> "Lv $min–$max"
    }
}
