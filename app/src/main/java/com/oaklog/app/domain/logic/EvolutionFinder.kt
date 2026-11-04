package com.oaklog.app.domain.logic

import com.oaklog.app.domain.model.EvoMethod
import com.oaklog.app.domain.model.EvolutionStep

/** Filtering over the flattened evolution list; kept pure so it can be tested without a network. */
object EvolutionFinder {

    /** Methods that actually occur, in the order they are offered as filters. */
    fun methods(steps: List<EvolutionStep>): List<EvoMethod> {
        val present = steps.map { it.method }.toSet()
        return EvoMethod.entries.filter { it in present }
    }

    /** Items involved in any step of [method] (or any method when null), alphabetical. */
    fun items(steps: List<EvolutionStep>, method: EvoMethod?): List<String> =
        steps.filter { method == null || it.method == method }
            .mapNotNull { it.item }
            .distinct()
            .sorted()

    /**
     * [query] matches either side of the evolution by name. [item] narrows to steps that use or hold
     * that item. Results keep Pokédex order of the evolved form.
     */
    fun filter(steps: List<EvolutionStep>, method: EvoMethod?, item: String?, query: String): List<EvolutionStep> {
        val q = query.trim().lowercase().replace('-', ' ')
        return steps.filter { s ->
            (method == null || s.method == method) &&
                (item == null || s.item == item) &&
                (q.isEmpty() ||
                    s.fromName.replace('-', ' ').contains(q) ||
                    s.toName.replace('-', ' ').contains(q))
        }.sortedWith(compareBy({ it.toId }, { it.fromId }))
    }
}
