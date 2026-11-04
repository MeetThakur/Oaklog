package com.oaklog.app.domain.logic

/** Reads PokéAPI's ball effect text, which states the catch multiplier in words. */
object BallInfo {
    private val multiplier = Regex("""(\d+(?:\.\d+)?)×""")
    private const val PREFIX = "Tries to catch a wild Pokémon."

    /** The first multiplier mentioned, like "1.5×", or null for balls whose text has none. */
    fun multiplier(effect: String?): String? = effect?.let { multiplier.find(it)?.value }

    /** The effect without the sentence every ball repeats, or null if nothing else is left. */
    fun detail(effect: String?): String? =
        effect?.removePrefix(PREFIX)?.trim()?.takeIf { it.isNotEmpty() }

    fun categoryLabel(category: String?): String = when (category) {
        "standard-balls" -> "Standard"
        "special-balls" -> "Special"
        "apricorn-balls" -> "Apricorn"
        else -> NameFormatter.pretty(category ?: "other")
    }
}
