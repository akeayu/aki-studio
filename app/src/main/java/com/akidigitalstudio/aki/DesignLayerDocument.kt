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
    val opacity: Float = 1f
) {
    fun validated(): DesignLayer {
        require(id.length in 1..64 && name.length in 1..48)
        require(kind in setOf("TEXT", "SHAPE"))
        require(shape in setOf("", "RECTANGLE", "ELLIPSE", "LINE"))
        require(text.length <= 500)
        require(listOf(x, y, width, height, strokeWidth, fontScale, rotationDegrees, opacity).all { it.isFinite() })
        require(x in 0f..1f && y in 0f..1f && width in 0.01f..1f && height in 0.01f..1f)
        require(strokeWidth in 0f..0.08f && fontScale in 0.01f..0.2f && rotationDegrees in -180f..180f && opacity in 0f..1f)
        require((kind == "TEXT" && text.isNotBlank()) || (kind == "SHAPE" && shape.isNotBlank()))
        return this
    }
}

object DesignLayerDocument {
    const val MAX_LAYERS = 30
    const val SCHEMA_VERSION = 2

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
            result += DesignLayer(
                id = input.readUTF(), name = input.readUTF(), kind = input.readUTF(),
                text = input.readUTF(), shape = input.readUTF(),
                x = input.readFloat(), y = input.readFloat(),
                width = input.readFloat(), height = input.readFloat(),
                fillColor = input.readInt(), strokeColor = input.readInt(),
                strokeWidth = input.readFloat(), fontScale = input.readFloat(),
                bold = input.readBoolean(), italic = input.readBoolean(),
                rotationDegrees = if (schema >= 2) input.readFloat() else 0f,
                visible = input.readBoolean(), locked = input.readBoolean(),
                opacity = input.readFloat()
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
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = layer.fillColor
                    this.alpha = (alpha * android.graphics.Color.alpha(layer.fillColor) / 255f).toInt()
                    textSize = layer.fontScale * shortEdge
                    typeface = Typeface.create(Typeface.SANS_SERIF,
                        (if (layer.bold) Typeface.BOLD else Typeface.NORMAL) or
                            (if (layer.italic) Typeface.ITALIC else Typeface.NORMAL))
                    textAlign = Paint.Align.CENTER
                }
                val lines = layer.text.split("\n").take(8)
                val lineHeight = paint.textSize * 1.18f
                val baseline = rect.centerY() - (lines.size - 1) * lineHeight / 2f - (paint.ascent() + paint.descent()) / 2f
                lines.forEachIndexed { index, line ->
                    canvas.drawText(line.take(80), rect.centerX(), baseline + index * lineHeight, paint)
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
                }
            }
            canvas.restore()
        }
        return output
    }
}
