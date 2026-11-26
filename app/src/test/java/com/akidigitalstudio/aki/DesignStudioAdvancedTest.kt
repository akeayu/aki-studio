package com.akidigitalstudio.aki

import org.junit.Assert.*
import org.junit.Test

class DesignStudioAdvancedTest {
    @Test fun parsesLineQuadraticCubicAndCloseCommands() {
        val commands = VectorPathEngine.parse("M 0.1 0.2 L 0.9 0.2 Q 0.5 0.1 0.5 0.8 C 0.4 0.9 0.2 0.9 0.1 0.8 Z")
        assertEquals(5, commands.size)
        assertTrue(commands[2] is VectorPathEngine.Command.Quad)
        assertTrue(commands[3] is VectorPathEngine.Command.Cubic)
        assertEquals(VectorPathEngine.Command.Close, commands.last())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPathCoordinatesOutsideNormalizedCanvas() {
        VectorPathEngine.parse("M 0.2 0.2 L 1.2 0.8")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMalformedPathCommands() {
        VectorPathEngine.parse("M 0.2 L 0.8 0.8")
    }

    @Test fun reorderMovesLayerToRequestedZOrder() {
        val a = DesignLayer(id = "a", name = "A", kind = "SHAPE", shape = "RECTANGLE")
        val b = DesignLayer(id = "b", name = "B", kind = "SHAPE", shape = "ELLIPSE")
        val c = DesignLayer(id = "c", name = "C", kind = "SHAPE", shape = "LINE")
        assertEquals(listOf("b", "c", "a"), DesignStudioOperations.reorder(listOf(a, b, c), "a", 2).map { it.id })
    }

    @Test fun groupTransformMovesAndRotatesSelectedLayersOnly() {
        val a = DesignLayer(id = "a", name = "A", kind = "SHAPE", shape = "RECTANGLE", x = 0.4f, y = 0.5f)
        val b = DesignLayer(id = "b", name = "B", kind = "SHAPE", shape = "ELLIPSE", x = 0.6f, y = 0.5f)
        val c = DesignLayer(id = "c", name = "C", kind = "SHAPE", shape = "LINE", x = 0.9f, y = 0.9f)
        val result = DesignStudioOperations.transformGroup(listOf(a, b, c), setOf("a", "b"), translateY = 0.1f, scale = 1.5f, rotateDegrees = 90f)
        assertEquals(0.5f, result[0].x, 0.001f)
        assertEquals(0.6f, result[0].y, 0.001f)
        assertEquals(0.5f, result[1].x, 0.001f)
        assertEquals(0.8f, result[1].y, 0.001f)
        assertEquals(c, result[2])
    }

    @Test fun objectSnappingAlignsCentersAndEdges() {
        val target = DesignLayer(id = "target", name = "Target", kind = "SHAPE", shape = "RECTANGLE", x = 0.5f, y = 0.5f)
        val moving = DesignLayer(id = "moving", name = "Moving", kind = "SHAPE", shape = "ELLIPSE", x = 0.51f, y = 0.51f)
        val snapped = DesignStudioOperations.snapToObjects(moving, listOf(target), 0.02f)
        assertTrue(snapped.snappedX)
        assertTrue(snapped.snappedY)
        assertEquals(0.5f, snapped.layer.x, 0.0001f)
        assertEquals(0.5f, snapped.layer.y, 0.0001f)
    }
}
