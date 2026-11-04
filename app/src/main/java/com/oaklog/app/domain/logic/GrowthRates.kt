package com.oaklog.app.domain.logic

/**
 * The six experience curves. PokéAPI names them "slow", "medium", "fast", "medium-slow",
 * "slow-then-very-fast" and "fast-then-very-slow"; the games call the last two Erratic and
 * Fluctuating. Experience to level 100 is a fixed constant of each curve.
 */
object GrowthRates {
    data class Curve(val label: String, val expToLevel100: Int)

    private val curves = mapOf(
        "slow" to Curve("Slow", 1_250_000),
        "medium" to Curve("Medium Fast", 1_000_000),
        "fast" to Curve("Fast", 800_000),
        "medium-slow" to Curve("Medium Slow", 1_059_860),
        "slow-then-very-fast" to Curve("Erratic", 600_000),
        "fast-then-very-slow" to Curve("Fluctuating", 1_640_000),
    )

    fun curve(slug: String?): Curve? = slug?.let { curves[it] }

    /** "Medium Slow · 1,059,860 EXP to Lv 100"; unknown slugs fall back to a prettified name. */
    fun describe(slug: String): String {
        val c = curve(slug) ?: return NameFormatter.pretty(slug)
        return "${c.label} · ${"%,d".format(c.expToLevel100)} EXP to Lv 100"
    }
}
