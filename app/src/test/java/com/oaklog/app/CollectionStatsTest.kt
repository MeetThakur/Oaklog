package com.oaklog.app

import com.oaklog.app.domain.logic.CollectionStats
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import org.junit.Assert.*
import org.junit.Test

class CollectionStatsTest {

    @Test
    fun cycleStatusTransitions() {
        var entry = CollectionEntry(speciesId = 25, status = CollectionStatus.NOT_CAUGHT)

        entry = CollectionStats.cycleStatus(entry)
        assertEquals(CollectionStatus.SEEN, entry.status)

        entry = CollectionStats.cycleStatus(entry)
        assertEquals(CollectionStatus.CAUGHT, entry.status)

        entry = CollectionStats.cycleStatus(entry)
        assertEquals(CollectionStatus.NOT_CAUGHT, entry.status)
        assertFalse(entry.shiny)
    }

    @Test
    fun toggleShinyForcesCaughtStatus() {
        val uncaught = CollectionEntry(speciesId = 25, status = CollectionStatus.NOT_CAUGHT, shiny = false)
        val shiny = CollectionStats.toggleShiny(uncaught)

        assertTrue(shiny.shiny)
        assertEquals(CollectionStatus.CAUGHT, shiny.status)

        val unShiny = CollectionStats.toggleShiny(shiny)
        assertFalse(unShiny.shiny)
        assertEquals(CollectionStatus.CAUGHT, unShiny.status)
    }
}

