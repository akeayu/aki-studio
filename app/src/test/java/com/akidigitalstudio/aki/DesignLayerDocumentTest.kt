package com.akidigitalstudio.aki

import org.junit.Assert.*
import org.junit.Test

class DesignLayerDocumentTest {
    @Test fun textAndShapeLayersRoundTrip() {
        val input = listOf(
            DesignLayer(name = "Headline", kind = "TEXT", text = "A New Way\nto Create", bold = true),
            DesignLayer(name = "Accent", kind = "SHAPE", shape = "ELLIPSE", fillColor = 0xFF00FF00.toInt())
        )
        val decoded = DesignLayerDocument.decode(DesignLayerDocument.encode(input))
        assertEquals(input, decoded)
    }

    @Test fun emptyDocumentIsSupportedForLegacyProjects() {
        assertTrue(DesignLayerDocument.decode(null).isEmpty())
        assertTrue(DesignLayerDocument.decode("").isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMoreThanThirtyLayers() {
        DesignLayerDocument.encode((1..31).map { DesignLayer(name = "Layer $it", kind = "SHAPE", shape = "RECTANGLE") })
    }

    @Test fun layerRecipeCanBeRestoredThroughEditHistory() {
        val before = DesignLayerDocument.encode(emptyList())
        val after = DesignLayerDocument.encode(listOf(DesignLayer(name = "Title", kind = "TEXT", text = "Aki Studio")))
        val history = EditHistory()
        history.reset(mapOf("designLayers" to before))
        history.record(mapOf("designLayers" to after))
        assertTrue(DesignLayerDocument.decode(history.undo()?.get("designLayers")).isEmpty())
        assertEquals("Aki Studio", DesignLayerDocument.decode(history.redo()?.get("designLayers")).single().text)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateLayerIdentifiers() {
        val layer = DesignLayer(id = "same", name = "One", kind = "SHAPE", shape = "RECTANGLE")
        DesignLayerDocument.encode(listOf(layer, layer.copy(name = "Two")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfBoundsLayerCoordinates() {
        DesignLayerDocument.encode(listOf(DesignLayer(name = "Bad", kind = "SHAPE", shape = "RECTANGLE", x = 1.2f)))
    }
}
