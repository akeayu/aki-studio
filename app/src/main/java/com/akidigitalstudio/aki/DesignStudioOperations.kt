package com.akidigitalstudio.aki

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Deterministic layer ordering, group transforms and object-to-object snapping. */
object DesignStudioOperations {
    data class SnapResult(val layer: DesignLayer, val snappedX: Boolean, val snappedY: Boolean)

    fun reorder(layers: List<DesignLayer>, layerId: String, targetIndex: Int): List<DesignLayer> {
        require(targetIndex in layers.indices) { "Target layer position is invalid" }
        val from = layers.indexOfFirst { it.id == layerId }
        require(from >= 0) { "Layer does not exist" }
        val result = layers.toMutableList()
        val item = result.removeAt(from)
        result.add(targetIndex.coerceIn(0, result.size), item)
        return result
    }

    /** Applies a common affine transform around the selected group's center. */
    fun transformGroup(
        layers: List<DesignLayer>,
        selectedIds: Set<String>,
        translateX: Float = 0f,
        translateY: Float = 0f,
        scale: Float = 1f,
        rotateDegrees: Float = 0f
    ): List<DesignLayer> {
        require(selectedIds.isNotEmpty() && selectedIds.size <= layers.size)
        require(layers.map { it.id }.containsAll(selectedIds))
        require(listOf(translateX, translateY, scale, rotateDegrees).all { it.isFinite() })
        require(translateX in -1f..1f && translateY in -1f..1f && scale in 0.1f..5f && rotateDegrees in -180f..180f)
        val selected = layers.filter { it.id in selectedIds }
        require(selected.none { it.locked }) { "Unlock selected layers before group transformation" }
        val left = selected.minOf { it.x - it.width / 2f }
        val right = selected.maxOf { it.x + it.width / 2f }
        val top = selected.minOf { it.y - it.height / 2f }
        val bottom = selected.maxOf { it.y + it.height / 2f }
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val radians = Math.toRadians(rotateDegrees.toDouble())
        val c = cos(radians).toFloat()
        val s = sin(radians).toFloat()
        return layers.map { layer ->
            if (layer.id !in selectedIds) layer else {
                val dx = layer.x - cx
                val dy = layer.y - cy
                val nx = cx + dx * c - dy * s + translateX
                val ny = cy + dx * s + dy * c + translateY
                layer.copy(
                    x = nx,
                    y = ny,
                    width = (layer.width * scale).coerceIn(0.01f, 1f),
                    height = (layer.height * scale).coerceIn(0.01f, 1f),
                    rotationDegrees = (layer.rotationDegrees + rotateDegrees).coerceIn(-180f, 180f)
                ).validated().also {
                    require(it.x in 0f..1f && it.y in 0f..1f) { "Group transform moves a layer outside the canvas" }
                }
            }
        }
    }

    /** Snaps a candidate layer center to nearby edges or centers of other visible objects. */
    fun snapToObjects(candidate: DesignLayer, others: List<DesignLayer>, tolerance: Float = 0.018f): SnapResult {
        require(tolerance.isFinite() && tolerance in 0f..0.1f)
        val peers = others.filter { it.id != candidate.id && it.visible }
        fun nearest(value: Float, targets: List<Float>): Float? =
            targets.map { it to abs(it - value) }.filter { it.second <= tolerance }.minByOrNull { it.second }?.first
        val xTargets = peers.flatMap { listOf(it.x, it.x - it.width / 2f + candidate.width / 2f, it.x + it.width / 2f - candidate.width / 2f) }
        val yTargets = peers.flatMap { listOf(it.y, it.y - it.height / 2f + candidate.height / 2f, it.y + it.height / 2f - candidate.height / 2f) }
        val sx = nearest(candidate.x, xTargets)
        val sy = nearest(candidate.y, yTargets)
        return SnapResult(candidate.copy(x = (sx ?: candidate.x).coerceIn(0f, 1f), y = (sy ?: candidate.y).coerceIn(0f, 1f)), sx != null, sy != null)
    }
}
