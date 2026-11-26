package com.akidigitalstudio.aki

/**
 * Bounded recipe history for non-destructive edits.
 *
 * Snapshots contain scalar edit parameters only; image bitmaps and source files
 * are never copied into the history. The caller owns serialization and rendering.
 */
class EditHistory(private val capacity: Int = DEFAULT_CAPACITY) {
    private val entries = mutableListOf<Map<String, String>>()
    private var cursor = -1

    init {
        require(capacity in 2..MAX_CAPACITY) { "History capacity must be between 2 and $MAX_CAPACITY" }
    }

    val canUndo: Boolean get() = cursor > 0
    val canRedo: Boolean get() = cursor >= 0 && cursor < entries.lastIndex
    val size: Int get() = entries.size
    val position: Int get() = cursor

    fun reset(initial: Map<String, String>) {
        entries.clear()
        entries.add(initial.toMap())
        cursor = 0
    }

    fun record(snapshot: Map<String, String>): Boolean {
        val safe = snapshot.toMap()
        if (cursor >= 0 && entries[cursor] == safe) return false
        while (entries.lastIndex > cursor) entries.removeAt(entries.lastIndex)
        entries.add(safe)
        cursor = entries.lastIndex
        if (entries.size > capacity) {
            entries.removeAt(0)
            cursor--
        }
        return true
    }

    fun undo(): Map<String, String>? {
        if (!canUndo) return null
        cursor--
        return entries[cursor].toMap()
    }

    fun redo(): Map<String, String>? {
        if (!canRedo) return null
        cursor++
        return entries[cursor].toMap()
    }

    companion object {
        const val DEFAULT_CAPACITY = 100
        const val MAX_CAPACITY = 100
    }
}
