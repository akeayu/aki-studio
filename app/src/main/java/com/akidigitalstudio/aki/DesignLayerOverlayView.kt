package com.akidigitalstudio.aki

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Touch editor for design objects. Geometry remains normalized to the source image.
 * A gesture is committed once on ACTION_UP so a drag consumes one history step.
 */
class DesignLayerOverlayView(context: Context) : View(context) {
    var sourceWidth: Int = 0
        private set
    var sourceHeight: Int = 0
        private set
    var layers: List<DesignLayer> = emptyList()
        private set
    var selectedLayerId: String? = null
        private set
    var onCommit: ((DesignLayer) -> Unit)? = null

    private val imageBounds = RectF()
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF75E0C0.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
    }
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCC75E0C0.toInt()
        strokeWidth = resources.displayMetrics.density
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val handleOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1C2B38.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * resources.displayMetrics.density
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF75E0C0.toInt()
        textSize = 12f * resources.displayMetrics.scaledDensity
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val selectedRect = RectF()
    private var working: DesignLayer? = null
    private var gestureMode = GestureMode.NONE
    private var downX = 0f
    private var downY = 0f
    private var downLocalX = 0f
    private var downLocalY = 0f
    private var startLayer: DesignLayer? = null
    private var startAngle = 0f
    private var startRotation = 0f
    private var resizeCorner = -1
    private var snappedX = false
    private var snappedY = false
    private var snapGuideX: Float? = null
    private var snapGuideY: Float? = null

    private enum class GestureMode { NONE, MOVE, RESIZE, ROTATE }

    init {
        isClickable = true
        contentDescription = "Select a design layer. Drag to move; use corner handles to resize and the top handle to rotate."
    }

    fun configure(width: Int, height: Int, items: List<DesignLayer>, selectedId: String?) {
        sourceWidth = width
        sourceHeight = height
        layers = items.toList()
        selectedLayerId = selectedId?.takeIf { id -> items.any { it.id == id } }
        visibility = if (width > 0 && height > 0 && items.isNotEmpty()) VISIBLE else GONE
        invalidate()
    }

    private fun fitBounds(): RectF {
        val scale = min(width.toFloat() / sourceWidth, height.toFloat() / sourceHeight)
        val w = sourceWidth * scale
        val h = sourceHeight * scale
        return RectF((width - w) / 2f, (height - h) / 2f, (width + w) / 2f, (height + h) / 2f)
    }

    private fun layerRect(layer: DesignLayer): RectF {
        val b = imageBounds
        val cx = b.left + layer.x * b.width()
        val cy = b.top + layer.y * b.height()
        val w = layer.width * b.width()
        val h = layer.height * b.height()
        return RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (sourceWidth <= 0 || sourceHeight <= 0) return
        imageBounds.set(fitBounds())
        val layer = working ?: layers.firstOrNull { it.id == selectedLayerId } ?: return
        if (!layer.visible) return
        selectedRect.set(layerRect(layer))
        canvas.save()
        canvas.rotate(layer.rotationDegrees, selectedRect.centerX(), selectedRect.centerY())
        canvas.drawRect(selectedRect, selectionPaint)
        val radius = 6f * resources.displayMetrics.density
        val points = arrayOf(
            selectedRect.left to selectedRect.top,
            selectedRect.right to selectedRect.top,
            selectedRect.left to selectedRect.bottom,
            selectedRect.right to selectedRect.bottom
        )
        points.forEach { (x, y) ->
            canvas.drawCircle(x, y, radius, handlePaint)
            canvas.drawCircle(x, y, radius, handleOutline)
        }
        val rotateY = selectedRect.top - 30f * resources.displayMetrics.density
        canvas.drawLine(selectedRect.centerX(), selectedRect.top, selectedRect.centerX(), rotateY, selectionPaint)
        canvas.drawCircle(selectedRect.centerX(), rotateY, radius + 1f, handlePaint)
        canvas.drawCircle(selectedRect.centerX(), rotateY, radius + 1f, handleOutline)
        canvas.drawText(layer.name, selectedRect.left, (selectedRect.top - 12f * resources.displayMetrics.density).coerceAtLeast(16f), labelPaint)
        canvas.restore()
        if (snappedX) snapGuideX?.let { value -> val x = imageBounds.left + value * imageBounds.width(); canvas.drawLine(x, imageBounds.top, x, imageBounds.bottom, guidePaint) }
        if (snappedY) snapGuideY?.let { value -> val y = imageBounds.top + value * imageBounds.height(); canvas.drawLine(imageBounds.left, y, imageBounds.right, y, guidePaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (sourceWidth <= 0 || sourceHeight <= 0) return false
                imageBounds.set(fitBounds())
                val hit = layers.asReversed().firstOrNull { it.visible && hitLayer(it, event.x, event.y) } ?: return false
                selectedLayerId = hit.id
                if (hit.locked) {
                    invalidate()
                    return true
                }
                working = hit
                startLayer = hit
                downX = event.x
                downY = event.y
                val rect = layerRect(hit)
                val centerX = rect.centerX()
                val centerY = rect.centerY()
                val local = inverseRotate(event.x, event.y, centerX, centerY, hit.rotationDegrees)
                downLocalX = local.first
                downLocalY = local.second
                val rotateY = rect.top - 30f * resources.displayMetrics.density
                val handle = 18f * resources.displayMetrics.density
                val corners = arrayOf(rect.left to rect.top, rect.right to rect.top, rect.left to rect.bottom, rect.right to rect.bottom)
                resizeCorner = corners.indices.minByOrNull { i -> distance(local.first, local.second, corners[i].first, corners[i].second) } ?: -1
                val cornerHit = resizeCorner >= 0 && distance(local.first, local.second, corners[resizeCorner].first, corners[resizeCorner].second) <= handle * 1.6f
                gestureMode = when {
                    distance(local.first, local.second, centerX, rotateY) <= handle * 1.6f -> GestureMode.ROTATE
                    cornerHit -> GestureMode.RESIZE
                    rect.contains(local.first, local.second) -> GestureMode.MOVE
                    else -> GestureMode.NONE
                }
                startAngle = angle(centerX, centerY, event.x, event.y)
                startRotation = hit.rotationDegrees
                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val base = startLayer ?: return false
                val dx = (event.x - downX) / imageBounds.width()
                val dy = (event.y - downY) / imageBounds.height()
                snappedX = false
                snappedY = false
                snapGuideX = null
                snapGuideY = null
                working = when (gestureMode) {
                    GestureMode.MOVE -> {
                        var nx = (base.x + dx).coerceIn(0f, 1f)
                        var ny = (base.y + dy).coerceIn(0f, 1f)
                        val objectSnap = DesignStudioOperations.snapToObjects(base.copy(x = nx, y = ny), layers, 0.018f)
                        nx = objectSnap.layer.x
                        ny = objectSnap.layer.y
                        snappedX = objectSnap.snappedX
                        snappedY = objectSnap.snappedY
                        if (snappedX) snapGuideX = nx
                        if (snappedY) snapGuideY = ny
                        if (!snappedX && kotlin.math.abs(nx - 0.5f) < 0.018f) { nx = 0.5f; snappedX = true; snapGuideX = nx }
                        if (!snappedY && kotlin.math.abs(ny - 0.5f) < 0.018f) { ny = 0.5f; snappedY = true; snapGuideY = ny }
                        base.copy(x = nx, y = ny)
                    }
                    GestureMode.RESIZE -> {
                        val right = resizeCorner == 1 || resizeCorner == 3
                        val bottom = resizeCorner == 2 || resizeCorner == 3
                        val rect = layerRect(base)
                        val localNow = inverseRotate(event.x, event.y, rect.centerX(), rect.centerY(), base.rotationDegrees)
                        val localDx = (localNow.first - downLocalX) / imageBounds.width()
                        val localDy = (localNow.second - downLocalY) / imageBounds.height()
                        val nw = (base.width + if (right) localDx else -localDx).coerceIn(0.01f, 1f)
                        val nh = (base.height + if (bottom) localDy else -localDy).coerceIn(0.01f, 1f)
                        val radians = Math.toRadians(base.rotationDegrees.toDouble())
                        val shiftX = (localDx * kotlin.math.cos(radians) - localDy * kotlin.math.sin(radians)) / 2f
                        val shiftY = (localDx * kotlin.math.sin(radians) + localDy * kotlin.math.cos(radians)) / 2f
                        base.copy(
                            x = (base.x + shiftX).coerceIn(0f, 1f),
                            y = (base.y + shiftY).coerceIn(0f, 1f),
                            width = nw,
                            height = nh
                        )
                    }
                    GestureMode.ROTATE -> {
                        val rect = layerRect(base)
                        val delta = angle(rect.centerX(), rect.centerY(), event.x, event.y) - startAngle
                        var degrees = (startRotation + delta).coerceIn(-180f, 180f)
                        val snapped = (degrees / 15f).roundToInt() * 15f
                        if (kotlin.math.abs(degrees - snapped) < 2.5f) degrees = snapped
                        base.copy(rotationDegrees = degrees)
                    }
                    GestureMode.NONE -> base
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val finalLayer = working
                if (event.actionMasked == MotionEvent.ACTION_UP && finalLayer != null && finalLayer != startLayer) {
                    onCommit?.invoke(finalLayer.validated())
                }
                working = null
                startLayer = null
                gestureMode = GestureMode.NONE
                resizeCorner = -1
                snappedX = false
                snappedY = false
                snapGuideX = null
                snapGuideY = null
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                performClick()
                return true
            }
        }
        return true
    }

    private fun hitLayer(layer: DesignLayer, x: Float, y: Float): Boolean {
        val rect = layerRect(layer)
        val local = inverseRotate(x, y, rect.centerX(), rect.centerY(), layer.rotationDegrees)
        val tolerance = 44f * resources.displayMetrics.density
        return RectF(rect.left - tolerance, rect.top - tolerance, rect.right + tolerance, rect.bottom + tolerance).contains(local.first, local.second)
    }

    private fun inverseRotate(x: Float, y: Float, cx: Float, cy: Float, degrees: Float): Pair<Float, Float> {
        val radians = Math.toRadians((-degrees).toDouble())
        val dx = x - cx
        val dy = y - cy
        val cosine = kotlin.math.cos(radians).toFloat()
        val sine = kotlin.math.sin(radians).toFloat()
        return (cx + dx * cosine - dy * sine) to (cy + dx * sine + dy * cosine)
    }

    private fun angle(cx: Float, cy: Float, x: Float, y: Float): Float =
        Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
