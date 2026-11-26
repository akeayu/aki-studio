package com.akidigitalstudio.aki

import org.junit.Assert.*
import org.junit.Test

class EditHistoryTest {
    @Test fun recordsAndUndoRedoRecipeSnapshots() {
        val history = EditHistory()
        val first = mapOf("rotation" to "0")
        val second = mapOf("rotation" to "90")
        val third = mapOf("rotation" to "180")
        history.reset(first)
        assertTrue(history.record(second))
        assertTrue(history.record(third))
        assertEquals(second, history.undo())
        assertEquals(third, history.redo())
    }

    @Test fun duplicateSnapshotsDoNotCreateHistoryEntries() {
        val history = EditHistory()
        val state = mapOf("brightness" to "10")
        history.reset(state)
        assertFalse(history.record(state))
        assertEquals(1, history.size)
    }

    @Test fun newEditAfterUndoDiscardsRedoBranch() {
        val history = EditHistory()
        history.reset(mapOf("crop" to "original"))
        history.record(mapOf("crop" to "square"))
        history.record(mapOf("crop" to "portrait"))
        history.undo()
        history.record(mapOf("crop" to "landscape"))
        assertFalse(history.canRedo)
        assertEquals(mapOf("crop" to "landscape"), history.undo()?.let { history.redo() })
    }

    @Test fun capacityRemainsBoundedAndOldestEntryIsDropped() {
        val history = EditHistory(capacity = 3)
        history.reset(mapOf("step" to "0"))
        for (step in 1..5) history.record(mapOf("step" to step.toString()))
        assertEquals(3, history.size)
        assertEquals(2, history.position)
        assertEquals(mapOf("step" to "4"), history.undo())
    }

    @Test fun snapshotsAreDefensivelyCopied() {
        val history = EditHistory()
        val mutable = mutableMapOf("rotation" to "0")
        history.reset(mutable)
        mutable["rotation"] = "270"
        history.record(mapOf("rotation" to "90"))
        assertEquals(mapOf("rotation" to "0"), history.undo())
    }

    @Test fun validatesCapacity() {
        assertThrows(IllegalArgumentException::class.java) { EditHistory(1) }
        assertThrows(IllegalArgumentException::class.java) { EditHistory(101) }
    }

    @Test fun emptyHistoryHasNoUndoOrRedo() {
        val history = EditHistory()
        assertNull(history.undo())
        assertNull(history.redo())
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }
}
