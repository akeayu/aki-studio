package com.akidigitalstudio.aki

import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** Compact layer panel with long-press drag-and-drop z-ordering. */
class LayerReorderPanel(
    context: Context,
    layers: List<DesignLayer>,
    private val onSelect: (String) -> Unit,
    private val onReorder: (List<DesignLayer>) -> Unit
) : LinearLayout(context) {
    private var current = layers.toList()

    init {
        orientation = VERTICAL
        setPadding(18, 12, 18, 12)
        rebuild()
    }

    private fun rebuild() {
        removeAllViews()
        current.asReversed().forEach { layer ->
            val row = TextView(context).apply {
                text = (if (layer.visible) "● " else "○ ") +
                    (if (layer.locked) "🔒 " else "") + layer.name + "  ·  " + layer.kind + "    ⠿"
                textSize = 15f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 14, 14, 14)
                minHeight = 52
                setBackgroundColor(0xFF1C2936.toInt())
                tag = layer.id
                contentDescription = "Layer ${layer.name}. Tap to edit. Long press and drag to reorder."
                setOnClickListener { onSelect(layer.id) }
                setOnLongClickListener {
                    val clip = ClipData.newPlainText("aki-layer-id", layer.id)
                    startDragAndDrop(clip, View.DragShadowBuilder(this), layer.id, 0)
                }
                setOnDragListener { target, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_STARTED -> true
                        DragEvent.ACTION_DRAG_ENTERED -> { target.alpha = 0.65f; true }
                        DragEvent.ACTION_DRAG_EXITED -> { target.alpha = 1f; true }
                        DragEvent.ACTION_DROP -> {
                            target.alpha = 1f
                            val sourceId = event.localState as? String
                            val targetId = target.tag as? String
                            if (sourceId != null && targetId != null && sourceId != targetId) {
                                val targetIndex = current.indexOfFirst { it.id == targetId }
                                if (targetIndex >= 0) {
                                    current = DesignStudioOperations.reorder(current, sourceId, targetIndex)
                                    rebuild()
                                    onReorder(current)
                                }
                            }
                            true
                        }
                        DragEvent.ACTION_DRAG_ENDED -> { target.alpha = 1f; true }
                        else -> true
                    }
                }
            }
            addView(row, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { bottomMargin = 6 })
        }
        addView(TextView(context).apply {
            text = "Tap a layer to edit. Long-press ⠿ and drag onto another layer to change stacking order."
            textSize = 12f
            setTextColor(0xFFB8C5D2.toInt())
            setPadding(4, 8, 4, 4)
        })
    }
}
