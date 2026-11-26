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

    @Test fun rotationIsPreservedBySchemaV2RoundTrip() {
        val layer = DesignLayer(name = "Rotated", kind = "SHAPE", shape = "RECTANGLE", rotationDegrees = 37.5f)
        val restored = DesignLayerDocument.decode(DesignLayerDocument.encode(listOf(layer))).single()
        assertEquals(37.5f, restored.rotationDegrees, 0.001f)
    }

    @Test fun schemaV1PayloadRemainsReadableWithZeroRotation() {
        val bytes = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(bytes).use { out ->
            out.writeInt(1); out.writeInt(1)
            out.writeUTF("legacy-id"); out.writeUTF("Legacy"); out.writeUTF("SHAPE")
            out.writeUTF(""); out.writeUTF("RECTANGLE")
            out.writeFloat(0.5f); out.writeFloat(0.5f)
            out.writeFloat(0.55f); out.writeFloat(0.18f)
            out.writeInt(0xFFFFFFFF.toInt()); out.writeInt(0xFF75E0C0.toInt())
            out.writeFloat(0.006f); out.writeFloat(0.055f)
            out.writeBoolean(false); out.writeBoolean(false)
            out.writeBoolean(true); out.writeBoolean(false); out.writeFloat(1f)
        }
        val encoded = java.util.Base64.getEncoder().encodeToString(bytes.toByteArray())
        val restored = DesignLayerDocument.decode(encoded).single()
        assertEquals("Legacy", restored.name)
        assertEquals(0f, restored.rotationDegrees, 0f)
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
    @Test fun schemaV2PayloadRetainsRotationAndDefaultsTypographyFields() {
        val bytes = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(bytes).use { out ->
            out.writeInt(2); out.writeInt(1)
            out.writeUTF("v2-id"); out.writeUTF("V2"); out.writeUTF("SHAPE")
            out.writeUTF(""); out.writeUTF("RECTANGLE")
            out.writeFloat(0.5f); out.writeFloat(0.5f)
            out.writeFloat(0.55f); out.writeFloat(0.18f)
            out.writeInt(0xFFFFFFFF.toInt()); out.writeInt(0xFF75E0C0.toInt())
            out.writeFloat(0.006f); out.writeFloat(0.055f)
            out.writeBoolean(false); out.writeBoolean(false)
            out.writeFloat(27f)
            out.writeBoolean(true); out.writeBoolean(false); out.writeFloat(1f)
        }
        val encoded = java.util.Base64.getEncoder().encodeToString(bytes.toByteArray())
        val restored = DesignLayerDocument.decode(encoded).single()
        assertEquals(27f, restored.rotationDegrees, 0f)
        assertEquals("SANS", restored.fontFamily)
        assertEquals("CENTER", restored.textAlignment)
        assertEquals("NONE", restored.textEffect)
    }

}
