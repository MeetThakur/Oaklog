package com.oaklog.app

import com.oaklog.app.domain.logic.GrowthRates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GrowthRatesTest {
    @Test
    fun `api names map to the game names and experience totals`() {
        assertEquals("Erratic", GrowthRates.curve("slow-then-very-fast")?.label)
        assertEquals(1_640_000, GrowthRates.curve("fast-then-very-slow")?.expToLevel100)
        assertEquals("Medium Fast", GrowthRates.curve("medium")?.label)
        assertNull(GrowthRates.curve("nonsense"))
    }

    @Test
    fun `description reads as one line`() {
        assertEquals("Medium Slow · 1,059,860 EXP to Lv 100", GrowthRates.describe("medium-slow").replace(' ', ' '))
    }

    @Test
    fun `unknown curve falls back to its name`() {
        assertEquals("Something New", GrowthRates.describe("something-new"))
    }
}
