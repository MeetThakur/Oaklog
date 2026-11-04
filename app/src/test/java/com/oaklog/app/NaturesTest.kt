package com.oaklog.app

import com.oaklog.app.domain.logic.Natures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NaturesTest {
    @Test
    fun `there are twenty five natures and five are neutral`() {
        assertEquals(25, Natures.all.size)
        assertEquals(5, Natures.all.count { it.isNeutral })
    }

    @Test
    fun `flavors follow the stats a nature raises and lowers`() {
        val adamant = Natures.byName("Adamant")
        assertEquals("Spicy", adamant.likes)
        assertEquals("Dry", adamant.dislikes)
        assertNull(Natures.byName("Hardy").likes)
    }

    @Test
    fun `each raise and lower pair is a single nature`() {
        assertEquals(listOf("Jolly"), Natures.matching("speed", "special-attack").map { it.name })
        assertEquals(4, Natures.matching("speed", null).size)
        assertEquals(25, Natures.matching(null, null).size)
    }
}
