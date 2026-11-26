package com.akidigitalstudio.aki

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Device-private reusable appearance presets. No account, sync, or network storage. */
data class DesignStylePreset(
    val name: String,
    val fillColor: Int,
    val strokeColor: Int,
    val strokeWidth: Float,
    val fontScale: Float,
    val bold: Boolean,
    val italic: Boolean,
    val opacity: Float,
    val textAlignment: String,
    val fontFamily: String,
    val lineSpacingMultiplier: Float,
    val textEffect: String,
    val effectColor: Int,
    val effectWidth: Float
) {
    fun applyTo(layer: DesignLayer): DesignLayer = layer.copy(
        fillColor = fillColor, strokeColor = strokeColor, strokeWidth = strokeWidth,
        fontScale = fontScale, bold = bold, italic = italic, opacity = opacity,
        textAlignment = textAlignment, fontFamily = fontFamily,
        lineSpacingMultiplier = lineSpacingMultiplier, textEffect = textEffect,
        effectColor = effectColor, effectWidth = effectWidth
    ).validated()

    fun toJson() = JSONObject().apply {
        put("name", name); put("fill", fillColor); put("stroke", strokeColor)
        put("strokeWidth", strokeWidth.toDouble()); put("fontScale", fontScale.toDouble())
        put("bold", bold); put("italic", italic); put("opacity", opacity.toDouble())
        put("alignment", textAlignment); put("font", fontFamily)
        put("lineSpacing", lineSpacingMultiplier.toDouble()); put("effect", textEffect)
        put("effectColor", effectColor); put("effectWidth", effectWidth.toDouble())
    }

    companion object {
        private const val PREFS = "aki_design_style_presets"
        private const val KEY = "custom"
        private val builtIns = listOf(
            DesignStylePreset("Aki Mint", 0xFFFFFFFF.toInt(), 0xFF75E0C0.toInt(), 0.006f, 0.055f, true, false, 1f, "CENTER", "SANS", 1.18f, "NONE", 0xFF0B1018.toInt(), 0.004f),
            DesignStylePreset("Editorial", 0xFFFFF4DF.toInt(), 0xFFDBA96B.toInt(), 0.004f, 0.062f, false, false, 1f, "LEFT", "SERIF", 1.28f, "SHADOW", 0x99000000.toInt(), 0.006f),
            DesignStylePreset("Bold Poster", 0xFFFFFFFF.toInt(), 0xFF72CFFF.toInt(), 0.012f, 0.075f, true, false, 1f, "CENTER", "CONDENSED", 1.05f, "OUTLINE", 0xFF10202C.toInt(), 0.008f),
            DesignStylePreset("Minimal", 0xFFE5EAF0.toInt(), 0xFF7C8A98.toInt(), 0.002f, 0.045f, false, false, 0.92f, "LEFT", "SANS", 1.2f, "NONE", 0xFF000000.toInt(), 0f)
        )

        fun all(context: Context): List<DesignStylePreset> {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
            val custom = runCatching {
                val array = JSONArray(raw)
                (0 until array.length()).mapNotNull { index ->
                    val o = array.optJSONObject(index) ?: return@mapNotNull null
                    DesignStylePreset(
                        o.getString("name"), o.getInt("fill"), o.getInt("stroke"),
                        o.getDouble("strokeWidth").toFloat(), o.getDouble("fontScale").toFloat(),
                        o.getBoolean("bold"), o.getBoolean("italic"), o.getDouble("opacity").toFloat(),
                        o.getString("alignment"), o.getString("font"), o.getDouble("lineSpacing").toFloat(),
                        o.getString("effect"), o.getInt("effectColor"), o.getDouble("effectWidth").toFloat()
                    )
                }
            }.getOrDefault(emptyList())
            return builtIns + custom
        }

        fun save(context: Context, preset: DesignStylePreset): Boolean {
            if (preset.name.isBlank() || preset.name.length > 32) return false
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val array = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
            val existing = (0 until array.length()).mapNotNull { array.optJSONObject(it) }
                .filterNot { it.optString("name").equals(preset.name, true) }
            if (existing.size >= 12) return false
            val updated = JSONArray()
            existing.forEach { updated.put(it) }
            updated.put(preset.toJson())
            return prefs.edit().putString(KEY, updated.toString()).commit()
        }

        fun fromLayer(name: String, layer: DesignLayer) = DesignStylePreset(
            name, layer.fillColor, layer.strokeColor, layer.strokeWidth, layer.fontScale,
            layer.bold, layer.italic, layer.opacity, layer.textAlignment, layer.fontFamily,
            layer.lineSpacingMultiplier, layer.textEffect, layer.effectColor, layer.effectWidth
        )
    }
}
