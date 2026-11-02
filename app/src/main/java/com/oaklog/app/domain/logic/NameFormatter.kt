package com.oaklog.app.domain.logic

/** Display formatting for PokéAPI slugs. Species names from the API are preferred when known. */
object NameFormatter {
    private val special = mapOf(
        "nidoran-f" to "Nidoran♀",
        "nidoran-m" to "Nidoran♂",
        "mr-mime" to "Mr. Mime",
        "mime-jr" to "Mime Jr.",
        "mr-rime" to "Mr. Rime",
        "farfetchd" to "Farfetch'd",
        "sirfetchd" to "Sirfetch'd",
        "type-null" to "Type: Null",
        "ho-oh" to "Ho-Oh",
        "porygon-z" to "Porygon-Z",
        "jangmo-o" to "Jangmo-o",
        "hakamo-o" to "Hakamo-o",
        "kommo-o" to "Kommo-o",
        "flabebe" to "Flabébé",
    )

    fun pretty(slug: String): String {
        special[slug]?.let { return it }
        return slug.split('-').joinToString(" ") { part -> part.replaceFirstChar { it.uppercaseChar() } }
    }

    fun dexNumber(id: Int): String = "#" + id.toString().padStart(4, '0')

    fun heightMeters(decimetres: Int): String = "%.1f m".format(decimetres / 10.0)
    fun weightKilograms(hectograms: Int): String = "%.1f kg".format(hectograms / 10.0)

    fun generationRoman(id: Int): String = when (id) {
        1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"; 6 -> "VI"; 7 -> "VII"; 8 -> "VIII"; 9 -> "IX"
        else -> id.toString()
    }

    /** "generation-iv" -> 4, or null if the slug isn't a generation slug. */
    fun generationFromSlug(slug: String?): Int? {
        val roman = slug?.removePrefix("generation-")?.uppercase() ?: return null
        return (1..20).firstOrNull { generationRoman(it) == roman }
    }

    /** Species id parsed from a PokéAPI url or pokemon form id; null for non-default forms. */
    fun cleanFlavor(text: String): String = text.replace('\n', ' ').replace('\u000c', ' ').replace("  ", " ").trim()
}
