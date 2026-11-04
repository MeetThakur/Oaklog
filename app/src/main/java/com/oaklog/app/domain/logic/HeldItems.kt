package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.HeldItem
import com.oaklog.app.domain.model.HeldRarity

object HeldItems {

    /** "50%" for one value, "5–50%" across several, or null when [game] has no entry. */
    fun rarityLabel(entries: List<HeldRarity>, game: String?): String? {
        val values = entries.filter { game == null || it.version == game }.map { it.percent }
        if (values.isEmpty()) return null
        val lo = values.min()
        val hi = values.max()
        return if (lo == hi) "$lo%" else "$lo–$hi%"
    }

    /** Items a Pokémon can hold in [game], or in any game when null. */
    fun forGame(items: List<HeldItem>, game: String?): List<HeldItem> =
        items.filter { item -> game == null || item.entries.any { it.version == game } }

    /** Games where anything is held, so a picker only offers meaningful choices. */
    fun games(items: List<HeldItem>): List<String> =
        items.flatMap { it.entries }.map { it.version }.distinct().sorted()
}
