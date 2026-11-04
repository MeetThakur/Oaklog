package com.oaklog.app.domain.logic

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

    fun levelRange(min: Int, max: Int): String = when {
        max <= 0 -> "—"
        min == max -> "Lv $min"
        else -> "Lv $min–$max"
    }
}
