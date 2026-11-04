package com.oaklog.app.data.repository

import com.oaklog.app.data.remote.dto.BerryDto
import com.oaklog.app.domain.model.BerryFlavor
import com.oaklog.app.domain.model.BerryInfo

internal object BerryMapper {
    fun info(dto: BerryDto) = BerryInfo(
        id = dto.id,
        name = dto.name,
        growthTime = dto.growthTime,
        maxHarvest = dto.maxHarvest,
        naturalGiftPower = dto.naturalGiftPower,
        naturalGiftType = dto.naturalGiftType?.name,
        size = dto.size,
        smoothness = dto.smoothness,
        soilDryness = dto.soilDryness,
        firmness = dto.firmness?.name,
        flavors = dto.flavors.filter { it.potency > 0 }.map { BerryFlavor(it.flavor.name, it.potency) }
            .sortedByDescending { it.potency },
        // Berry names omit the suffix their item carries: "cheri" is the item "cheri-berry".
        itemName = dto.item?.name ?: "${dto.name}-berry",
    )
}
