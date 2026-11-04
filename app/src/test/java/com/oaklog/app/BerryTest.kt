package com.oaklog.app

import com.oaklog.app.data.remote.dto.BerryDto
import com.oaklog.app.data.remote.dto.BerryFlavorDto
import com.oaklog.app.data.remote.dto.NamedRefDto
import com.oaklog.app.data.repository.BerryMapper
import com.oaklog.app.domain.logic.BerryFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class BerryTest {
    private fun dto(id: Int, name: String, vararg flavors: Pair<String, Int>) = BerryDto(
        id = id, name = name, growthTime = 3,
        flavors = flavors.map { BerryFlavorDto(it.second, NamedRefDto(it.first)) },
        item = NamedRefDto("$name-berry"),
    )

    private val berries = listOf(
        BerryMapper.info(dto(1, "cheri", "spicy" to 10, "dry" to 0)),
        BerryMapper.info(dto(2, "chesto", "dry" to 10)),
        BerryMapper.info(dto(3, "pecha", "sweet" to 10)),
        BerryMapper.info(dto(4, "razz", "spicy" to 15, "dry" to 15)),
    )

    @Test
    fun `zero potency flavors are dropped`() {
        assertEquals(listOf("spicy"), berries.first().flavors.map { it.name })
    }

    @Test
    fun `flavor filter keeps berries with it, strongest first`() {
        assertEquals(listOf("razz", "cheri"), BerryFilter.filter(berries, "spicy", "").map { it.name })
    }

    @Test
    fun `query matches the name and flavors list keeps a sensible order`() {
        assertEquals(listOf("chesto"), BerryFilter.filter(berries, null, "chest").map { it.name })
        assertEquals(listOf("spicy", "dry", "sweet"), BerryFilter.flavors(berries))
    }

    @Test
    fun `ripening is four growth stages`() {
        assertEquals(12, BerryFilter.hoursToRipen(berries.first()))
    }
}
