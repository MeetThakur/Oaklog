package com.oaklog.app.data.repository

import com.oaklog.app.data.remote.dto.ChainLinkDto
import com.oaklog.app.data.remote.dto.EvolutionDetailDto
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.EvoMethod
import com.oaklog.app.domain.model.EvolutionStep

internal object EvolutionMapper {

    /** Every edge of the chain, depth first. */
    fun steps(root: ChainLinkDto): List<EvolutionStep> = buildList {
        fun walk(link: ChainLinkDto) {
            link.evolvesTo.forEach { child ->
                // A child with no listed details still evolves; keep it as an "Other" step.
                val details = child.evolutionDetails.ifEmpty { listOf(EvolutionDetailDto()) }
                details.forEach { d ->
                    add(
                        EvolutionStep(
                            fromId = link.species.id ?: 0,
                            fromName = link.species.name,
                            toId = child.species.id ?: 0,
                            toName = child.species.name,
                            method = method(d),
                            item = (d.item ?: d.heldItem)?.name,
                            requirement = requirement(d),
                        ),
                    )
                }
                walk(child)
            }
        }
        walk(root)
    }

    /** The one category a step is filed under; an evolution stone beats everything else it requires. */
    fun method(d: EvolutionDetailDto): EvoMethod = when {
        d.item != null -> EvoMethod.ITEM
        d.trigger?.name == "trade" -> EvoMethod.TRADE
        d.heldItem != null -> EvoMethod.HELD_ITEM
        d.knownMove != null || d.knownMoveType != null -> EvoMethod.MOVE
        d.location != null -> EvoMethod.LOCATION
        d.minHappiness != null -> EvoMethod.HAPPINESS
        d.minLevel != null -> EvoMethod.LEVEL
        else -> EvoMethod.OTHER
    }

    fun requirement(d: EvolutionDetailDto): String {
        val parts = mutableListOf<String>()
        if (d.minLevel != null) parts.add("Level ${d.minLevel}")
        if (d.item != null) parts.add("Use ${NameFormatter.pretty(d.item.name)}")
        if (d.minHappiness != null) parts.add("Happiness ${d.minHappiness}+")
        if (d.heldItem != null) parts.add("Hold ${NameFormatter.pretty(d.heldItem.name)}")
        if (d.knownMove != null) parts.add("Knows ${NameFormatter.pretty(d.knownMove.name)}")
        if (!d.timeOfDay.isNullOrBlank()) parts.add(d.timeOfDay.replaceFirstChar { it.uppercase() })
        if (d.location != null) parts.add("at ${NameFormatter.pretty(d.location.name)}")
        if (d.needsOverworldRain) parts.add("During Rain")
        if (d.turnUpsideDown) parts.add("Turn Device Upside Down")
        if (d.tradeSpecies != null) parts.add("Trade with ${NameFormatter.pretty(d.tradeSpecies.name)}")
        if (d.trigger?.name == "trade" && d.heldItem == null && d.tradeSpecies == null) parts.add("Trade")
        if (parts.isEmpty()) parts.add(NameFormatter.pretty(d.trigger?.name ?: "Level Up"))
        return parts.joinToString(", ")
    }
}
