package com.akidigitalstudio.aki

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

/** Accessible, dependency-free color palette with an explicit hex entry fallback. */
object LayerColorPicker {
    private val palette = intArrayOf(
        0xFFFFFFFF.toInt(), 0xFF0B1018.toInt(), 0xFF75E0C0.toInt(), 0xFF72CFFF.toInt(),
        0xFFFFC857.toInt(), 0xFFFF7B72.toInt(), 0xFFB69CFF.toInt(), 0xFFFFA6D6.toInt(),
        0xFF9BE37A.toInt(), 0xFF8A9BAE.toInt(), 0xFF3B5368.toInt(), 0xFF222222.toInt(),
        0xFFE5EAF0.toInt(), 0xFFDBA96B.toInt(), 0xFF6D7DFF.toInt(), 0xFF5A8F7B.toInt()
    )

    fun show(context: Context, initial: Int, onSelected: (Int) -> Unit) {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 12, 24, 4)
        }
        val hex = EditText(context).apply {
            hint = "#RRGGBB or #AARRGGBB"
            setSingleLine(true)
            setText(String.format("#%06X", 0xFFFFFF and initial))
        }
        root.addView(TextView(context).apply { text = "Palette"; textSize = 13f })
        val grid = GridLayout(context).apply { columnCount = 4 }
        var selected = initial
        palette.forEach { color ->
            val swatch = TextView(context).apply {
                text = "●"
                textSize = 28f
                gravity = android.view.Gravity.CENTER
                setTextColor(color)
                contentDescription = String.format("#%08X", color)
                isClickable = true
                isFocusable = true
                minimumWidth = 52
                minimumHeight = 48
                setOnClickListener {
                    selected = color
                    hex.setText(String.format("#%08X", color))
                }
            }
            grid.addView(swatch, GridLayout.LayoutParams().apply {
                width = 0
                height = 52
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }
        root.addView(grid)
        root.addView(TextView(context).apply { text = "Custom color"; textSize = 13f; setPadding(0, 12, 0, 2) })
        root.addView(hex)
        AlertDialog.Builder(context)
            .setTitle("Choose color")
            .setView(root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Apply", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val parsed = runCatching { Color.parseColor(hex.text.toString().trim()) }.getOrNull()
                        if (parsed == null) {
                            hex.error = "Enter a valid color such as #75E0C0"
                            return@setOnClickListener
                        }
                        selected = parsed
                        onSelected(selected)
                        dialog.dismiss()
                    }
                }
            }.show()
    }
}
