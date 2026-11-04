package com.oaklog.app.domain.logic

/** Stat keys as published by PokéAPI, in the order the games list them. */
val STAT_KEYS = listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed")

data class Nature(val name: String, val up: String?, val down: String?) {
    val isNeutral: Boolean get() = up == null || down == null
}

object Natures {
    private fun n(name: String, up: String?, down: String?) = Nature(name, up, down)

    val all: List<Nature> = listOf(
        n("Hardy", null, null), n("Docile", null, null), n("Serious", null, null),
        n("Bashful", null, null), n("Quirky", null, null),
        n("Lonely", "attack", "defense"), n("Brave", "attack", "speed"),
        n("Adamant", "attack", "special-attack"), n("Naughty", "attack", "special-defense"),
        n("Bold", "defense", "attack"), n("Relaxed", "defense", "speed"),
        n("Impish", "defense", "special-attack"), n("Lax", "defense", "special-defense"),
        n("Timid", "speed", "attack"), n("Hasty", "speed", "defense"),
        n("Jolly", "speed", "special-attack"), n("Naive", "speed", "special-defense"),
        n("Modest", "special-attack", "attack"), n("Mild", "special-attack", "defense"),
        n("Quiet", "special-attack", "speed"), n("Rash", "special-attack", "special-defense"),
        n("Calm", "special-defense", "attack"), n("Gentle", "special-defense", "defense"),
        n("Sassy", "special-defense", "speed"), n("Careful", "special-defense", "special-attack"),
    )

    val neutral: Nature = all.first()

    fun byName(name: String): Nature = all.firstOrNull { it.name == name } ?: neutral
}

/** Level-based stat formulas from the main-series games (generation 3 onward). */
object StatCalc {
    const val MAX_IV = 31
    const val MAX_EV = 252
    const val MAX_TOTAL_EV = 510

    fun hp(base: Int, iv: Int, ev: Int, level: Int): Int =
        // Shedinja-style base-1 HP is always 1.
        if (base == 1) 1 else (2 * base + iv + ev / 4) * level / 100 + level + 10

    fun other(base: Int, iv: Int, ev: Int, level: Int, nature: Nature, statKey: String): Int {
        val raw = (2 * base + iv + ev / 4) * level / 100 + 5
        return when (statKey) {
            nature.up -> raw * 110 / 100
            nature.down -> raw * 90 / 100
            else -> raw
        }
    }

    fun stat(key: String, base: Int, iv: Int, ev: Int, level: Int, nature: Nature): Int =
        if (key == "hp") hp(base, iv, ev, level) else other(base, iv, ev, level, nature, key)

    /** Speed at a level with a neutral nature and the given IV/EV. */
    fun speed(base: Int, level: Int, iv: Int = MAX_IV, ev: Int = 0, nature: Nature = Natures.neutral): Int =
        other(base, iv, ev, level, nature, "speed")
}

data class DamageResult(
    val min: Int,
    val max: Int,
    val minPercent: Double,
    val maxPercent: Double,
    val effectiveness: Double,
    val stab: Boolean,
) {
    /** Hits needed to faint the target, as a (best case, worst case) pair, or null if it can't. */
    val hitsToKo: Pair<Int, Int>? get() =
        if (max <= 0) null else Pair(ceilDiv(100.0, maxPercent), ceilDiv(100.0, minPercent.coerceAtLeast(0.01)))

    private fun ceilDiv(a: Double, b: Double): Int = kotlin.math.ceil(a / b - 1e-9).toInt().coerceAtLeast(1)
}

/** Single-target damage from the main-series formula, without items, abilities or weather. */
object DamageCalc {
    fun calculate(
        level: Int,
        power: Int,
        attack: Int,
        defense: Int,
        defenderHp: Int,
        moveType: String,
        attackerTypes: List<String>,
        defenderTypes: List<String>,
        chart: TypeChart,
        critical: Boolean = false,
    ): DamageResult {
        val effectiveness = chart.multiplier(moveType, defenderTypes)
        val stab = moveType in attackerTypes
        if (power <= 0 || effectiveness == 0.0 || defense <= 0) {
            return DamageResult(0, 0, 0.0, 0.0, effectiveness, stab)
        }
        val base = (2 * level / 5 + 2) * power * attack / defense / 50 + 2
        fun roll(random: Int): Int {
            var d = base.toDouble()
            if (critical) d = kotlin.math.floor(d * 1.5)
            d = kotlin.math.floor(d * random / 100)
            if (stab) d = kotlin.math.floor(d * 1.5)
            d = kotlin.math.floor(d * effectiveness)
            return d.toInt().coerceAtLeast(1)
        }
        val min = roll(85)
        val max = roll(100)
        val hp = defenderHp.coerceAtLeast(1).toDouble()
        return DamageResult(min, max, min / hp * 100, max / hp * 100, effectiveness, stab)
    }
}
