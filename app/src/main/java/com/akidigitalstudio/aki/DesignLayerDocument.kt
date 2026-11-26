package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.util.UUID

/** Serializable, non-destructive design objects in normalized canvas coordinates. */
data class DesignLayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kind: String,
    val text: String = "",
    val shape: String = "",
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val width: Float = 0.55f,
    val height: Float = 0.18f,
    val fillColor: Int = 0xFFFFFFFF.toInt(),
    val strokeColor: Int = 0xFF75E0C0.toInt(),
    val strokeWidth: Float = 0.006f,
    val fontScale: Float = 0.055f,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val rotationDegrees: Float = 0f,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val opacity: Float = 1f,
    val textAlignment: String = "CENTER",
    val fontFamily: String = "SANS",
    val lineSpacingMultiplier: Float = 1.18f,
    val textEffect: String = "NONE",
    val effectColor: Int = 0xFF000000.toInt(),
    val effectWidth: Float = 0.004f,
    val pathData: String = ""
) {
    fun validated(): DesignLayer {
        require(id.length in 1..64 && name.length in 1..48)
        require(kind in setOf("TEXT", "SHAPE"))
        require(shape in setOf("", "RECTANGLE", "ELLIPSE", "LINE", "CUSTOM_PATH"))
        require(text.length <= 500 && pathData.length <= 2048)
        require(listOf(x, y, width, height, strokeWidth, fontScale, rotationDegrees, opacity,
            lineSpacingMultiplier, effectWidth).all { it.isFinite() })
        require(x in 0f..1f && y in 0f..1f && width in 0.01f..1f && height in 0.01f..1f)
        require(strokeWidth in 0f..0.08f && fontScale in 0.01f..0.2f && rotationDegrees in -180f..180f && opacity in 0f..1f)
        require(textAlignment in setOf("LEFT", "CENTER", "RIGHT"))
        require(fontFamily in setOf("SANS", "SERIF", "MONOSPACE", "CONDENSED", "MEDIUM", "ETHIOPIC_SYSTEM"))
        require(lineSpacingMultiplier in 0.8f..2f)
        require(textEffect in setOf("NONE", "SHADOW", "OUTLINE"))
        require(effectWidth in 0f..0.03f)
        require((kind == "TEXT" && text.isNotBlank()) ||
            (kind == "SHAPE" && (shape.isNotBlank() && (shape != "CUSTOM_PATH" || pathData.isNotBlank()))))
        if (kind == "SHAPE" && shape == "CUSTOM_PATH") VectorPathEngine.parse(pathData)
        return this
    }
}

object DesignLayerDocument {
    const val MAX_LAYERS = 30
    const val SCHEMA_VERSION = 3

    /** Compact, bounded, dependency-free serialization for Android and local JVM tests. */
    fun encode(layers: List<DesignLayer>): String {
        require(layers.size <= MAX_LAYERS) { "A design can contain at most $MAX_LAYERS layers" }
        require(layers.map { it.id }.distinct().size == layers.size) { "Layer identifiers must be unique" }
        val bytes = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(bytes).use { out ->
            out.writeInt(SCHEMA_VERSION)
            out.writeInt(layers.size)
            layers.forEach { raw ->
                val l = raw.validated()
                out.writeUTF(l.id); out.writeUTF(l.name); out.writeUTF(l.kind)
                out.writeUTF(l.text); out.writeUTF(l.shape)
                out.writeFloat(l.x); out.writeFloat(l.y)
                out.writeFloat(l.width); out.writeFloat(l.height)
                out.writeInt(l.fillColor); out.writeInt(l.strokeColor)
                out.writeFloat(l.strokeWidth); out.writeFloat(l.fontScale)
                out.writeBoolean(l.bold); out.writeBoolean(l.italic)
                out.writeFloat(l.rotationDegrees)
                out.writeBoolean(l.visible); out.writeBoolean(l.locked)
                out.writeFloat(l.opacity)
                out.writeUTF(l.textAlignment); out.writeUTF(l.fontFamily)
                out.writeFloat(l.lineSpacingMultiplier); out.writeUTF(l.textEffect)
                out.writeInt(l.effectColor); out.writeFloat(l.effectWidth)
                out.writeUTF(l.pathData)
            }
        }
        return java.util.Base64.getEncoder().encodeToString(bytes.toByteArray())
    }

    fun decode(serialized: String?): MutableList<DesignLayer> {
        if (serialized.isNullOrBlank()) return mutableListOf()
        require(serialized.length <= 128_000) { "Design layer document is too large" }
        val bytes = try { java.util.Base64.getDecoder().decode(serialized) }
            catch (_: IllegalArgumentException) { throw IllegalArgumentException("Invalid design layer encoding") }
        val input = java.io.DataInputStream(bytes.inputStream())
        val schema = input.readInt()
        require(schema in 1..SCHEMA_VERSION) { "Unsupported design layer schema" }
        val count = input.readInt()
        require(count in 0..MAX_LAYERS) { "Invalid design layer count" }
        val result = mutableListOf<DesignLayer>()
        repeat(count) {
            val id = input.readUTF()
            val name = input.readUTF()
            val kind = input.readUTF()
            val text = input.readUTF()
            val shape = input.readUTF()
            val x = input.readFloat(); val y = input.readFloat()
            val width = input.readFloat(); val height = input.readFloat()
            val fill = input.readInt(); val stroke = input.readInt()
            val strokeWidth = input.readFloat(); val fontScale = input.readFloat()
            val bold = input.readBoolean(); val italic = input.readBoolean()
            val rotation = if (schema >= 2) input.readFloat() else 0f
            val visible = input.readBoolean(); val locked = input.readBoolean()
            val opacity = input.readFloat()
            val alignment = if (schema >= 3) input.readUTF() else "CENTER"
            val family = if (schema >= 3) input.readUTF() else "SANS"
            val lineSpacing = if (schema >= 3) input.readFloat() else 1.18f
            val effect = if (schema >= 3) input.readUTF() else "NONE"
            val effectColor = if (schema >= 3) input.readInt() else 0xFF000000.toInt()
            val effectWidth = if (schema >= 3) input.readFloat() else 0.004f
            val pathData = if (schema >= 3) input.readUTF() else ""
            result += DesignLayer(
                id = id, name = name, kind = kind, text = text, shape = shape,
                x = x, y = y, width = width, height = height,
                fillColor = fill, strokeColor = stroke, strokeWidth = strokeWidth, fontScale = fontScale,
                bold = bold, italic = italic, rotationDegrees = rotation,
                visible = visible, locked = locked, opacity = opacity,
                textAlignment = alignment, fontFamily = family, lineSpacingMultiplier = lineSpacing,
                textEffect = effect, effectColor = effectColor, effectWidth = effectWidth, pathData = pathData
            ).validated()
        }
        require(input.available() == 0) { "Unexpected data after design layer document" }
        require(result.map { it.id }.distinct().size == result.size) { "Duplicate layer identifiers" }
        return result
    }
}

/** CPU renderer; design layers are composited on a fresh output bitmap, never the source. */
object DesignLayerRenderer {
    fun composite(source: Bitmap, layers: List<DesignLayer>): Bitmap {
        if (layers.none { it.visible }) return source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not copy design canvas")
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not allocate design canvas")
        val canvas = Canvas(output)
        val shortEdge = minOf(source.width, source.height).toFloat()
        layers.filter { it.visible }.forEach { raw ->
            val layer = raw.validated()
            val left = (layer.x - layer.width / 2f) * source.width
            val top = (layer.y - layer.height / 2f) * source.height
            val rect = RectF(left, top, left + layer.width * source.width, top + layer.height * source.height)
            val alpha = (layer.opacity * 255f).toInt().coerceIn(0, 255)
            canvas.save()
            canvas.rotate(layer.rotationDegrees, rect.centerX(), rect.centerY())
            if (layer.kind == "TEXT") {
                val style = (if (layer.bold) Typeface.BOLD else Typeface.NORMAL) or
                    (if (layer.italic) Typeface.ITALIC else Typeface.NORMAL)
                val typeface = when (layer.fontFamily) {
                    "SERIF" -> Typeface.create(Typeface.SERIF, style)
                    "MONOSPACE" -> Typeface.create(Typeface.MONOSPACE, style)
                    "CONDENSED" -> Typeface.create("sans-serif-condensed", style)
                    "MEDIUM" -> Typeface.create("sans-serif-medium", style)
                    "ETHIOPIC_SYSTEM" -> Typeface.create("sans-serif", style)
                    else -> Typeface.create(Typeface.SANS_SERIF, style)
                }
                val textPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = layer.fillColor
                    alpha = (alpha * android.graphics.Color.alpha(layer.fillColor) / 255f).toInt()
                    textSize = layer.fontScale * shortEdge
                    this.typeface = typeface
                    textAlign = Paint.Align.LEFT
                }
                val alignment = when (layer.textAlignment) {
                    "CENTER" -> android.text.Layout.Alignment.ALIGN_CENTER
                    "RIGHT" -> android.text.Layout.Alignment.ALIGN_OPPOSITE
                    else -> android.text.Layout.Alignment.ALIGN_NORMAL
                }
                val layoutWidth = maxOf(1, rect.width().toInt())
                val layout = android.text.StaticLayout.Builder
                    .obtain(layer.text, 0, layer.text.length, textPaint, layoutWidth)
                    .setAlignment(alignment)
                    .setTextDirection(android.text.TextDirectionHeuristics.FIRSTSTRONG_LTR)
                    .setLineSpacing(0f, layer.lineSpacingMultiplier)
                    .setIncludePad(false)
                    .setMaxLines(8)
                    .build()
                canvas.save()
                canvas.clipRect(rect)
                canvas.translate(rect.left, rect.centerY() - layout.height / 2f)
                when (layer.textEffect) {
                    "SHADOW" -> {
                        textPaint.setShadowLayer(layer.effectWidth * shortEdge, 0f, layer.effectWidth * shortEdge, layer.effectColor)
                        layout.draw(canvas)
                        textPaint.clearShadowLayer()
                    }
                    "OUTLINE" -> {
                        textPaint.color = layer.effectColor
                        textPaint.alpha = (alpha * android.graphics.Color.alpha(layer.effectColor) / 255f).toInt()
                        textPaint.style = Paint.Style.STROKE
                        textPaint.strokeWidth = maxOf(1f, layer.effectWidth * shortEdge)
                        layout.draw(canvas)
                        textPaint.color = layer.fillColor
                        textPaint.alpha = (alpha * android.graphics.Color.alpha(layer.fillColor) / 255f).toInt()
                        textPaint.style = Paint.Style.FILL
                        layout.draw(canvas)
                    }
                    else -> layout.draw(canvas)
                }
                canvas.restore()
            } else {
                        canvas.drawText(line.take(80), textX, y, paint)
                    }
                }
                canvas.restore()
            } else {
                val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = layer.fillColor
                    this.alpha = (alpha * android.graphics.Color.alpha(layer.fillColor) / 255f).toInt()
                    style = Paint.Style.FILL
                }
                val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = layer.strokeColor
                    this.alpha = (alpha * android.graphics.Color.alpha(layer.strokeColor) / 255f).toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = layer.strokeWidth * shortEdge
                }
                when (layer.shape) {
                    "RECTANGLE" -> { canvas.drawRect(rect, fill); if (stroke.strokeWidth > 0f) canvas.drawRect(rect, stroke) }
                    "ELLIPSE" -> { canvas.drawOval(rect, fill); if (stroke.strokeWidth > 0f) canvas.drawOval(rect, stroke) }
                    "LINE" -> canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), stroke)
                    "CUSTOM_PATH" -> {
                        val path = VectorPathEngine.toAndroidPath(layer.pathData, rect)
                        if (android.graphics.Color.alpha(layer.fillColor) > 0) canvas.drawPath(path, fill)
                        if (stroke.strokeWidth > 0f) canvas.drawPath(path, stroke)
                    }
                }
            }
            canvas.restore()
        }
        return output
    }
}
