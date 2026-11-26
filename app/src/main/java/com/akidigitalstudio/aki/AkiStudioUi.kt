package com.akidigitalstudio.aki

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/** Lightweight reusable visual system built only with Android platform Views. */
object AkiStudioUi {
    const val INK = 0xFF0B1018.toInt()
    const val SURFACE = 0xFF141D29.toInt()
    const val SURFACE_RAISED = 0xFF1B2836.toInt()
    const val OUTLINE = 0xFF2A3A4A.toInt()
    const val TEXT = 0xFFF2F6F8.toInt()
    const val MUTED = 0xFFA7B6C4.toInt()
    const val MINT = 0xFF75E0C0.toInt()
    const val CYAN = 0xFF72CFFF.toInt()

    data class Action(val symbol: String, val title: String, val subtitle: String, val onClick: () -> Unit)

    fun page(context: Context, profile: AdaptiveUiProfile = AdaptiveUiProfile.from(context)) = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(context, profile.pagePaddingDp), dp(context, profile.pagePaddingDp + 4), dp(context, profile.pagePaddingDp), dp(context, profile.pagePaddingDp + 12))
        setBackgroundColor(INK)
        clipChildren = false
        clipToPadding = false
    }

    fun header(context: Context, english: Boolean, languageAction: () -> Unit): View {
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 2), dp(context, 4), dp(context, 2), dp(context, 12))
        }
        val top = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        top.addView(TextView(context).apply {
            text = "AKI  /  STUDIO"
            textSize = 23f
            letterSpacing = 0.08f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(TEXT)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(TextView(context).apply {
            text = if (english) "አማርኛ" else "ENGLISH"
            textSize = 11f
            letterSpacing = 0.04f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(MINT)
            background = rounded(context, SURFACE_RAISED, OUTLINE, 18f)
            setPadding(dp(context, 13), dp(context, 9), dp(context, 13), dp(context, 9))
            minimumHeight = dp(context, 48)
            minimumWidth = dp(context, 64)
            contentDescription = if (english) "Switch language to Amharic" else "Switch language to English"
            isClickable = true
            isFocusable = true
            setOnClickListener { languageAction() }
        })
        column.addView(top)
        column.addView(TextView(context).apply {
            text = if (english) "A New Way to Create" else "አዲስ የፈጠራ መንገድ"
            textSize = 14f
            setTextColor(MUTED)
            setPadding(0, dp(context, 5), 0, dp(context, 12))
        })
        val meta = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        meta.addView(badge(context, "●  " + if (english) "ON-DEVICE STUDIO" else "በመሣሪያው ላይ", MINT))
        meta.addView(horizontalSpace(context, 8))
        meta.addView(badge(context, "◈  " + if (english) "PRIVATE WORKSPACE" else "የግል የሥራ ቦታ", CYAN))
        column.addView(meta)
        return column
    }

    fun badge(context: Context, label: String, color: Int = MINT) = TextView(context).apply {
        text = label
        textSize = 9f
        letterSpacing = 0.06f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(color)
        background = rounded(context, SURFACE_RAISED, OUTLINE, 14f)
        setPadding(dp(context, 9), dp(context, 6), dp(context, 9), dp(context, 6))
    }

    fun panel(context: Context, title: String, subtitle: String, actions: List<Action>, english: Boolean, profile: AdaptiveUiProfile = AdaptiveUiProfile.from(context)): LinearLayout {
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(context, SURFACE, OUTLINE, 20f)
            setPadding(dp(context, 13), dp(context, 13), dp(context, 13), dp(context, 12))
        }
        val heading = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val copy = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        copy.addView(TextView(context).apply {
            text = title
            textSize = 16f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(TEXT)
        })
        copy.addView(TextView(context).apply {
            text = subtitle
            textSize = 11f
            setTextColor(MUTED)
            setPadding(0, dp(context, 3), 0, 0)
        })
        heading.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        heading.addView(badge(context, actions.size.toString().padStart(2, '0') + " " + if (english) "TOOLS" else "መሣሪያዎች", CYAN))
        panel.addView(heading)
        panel.addView(space(context, 12))
        panel.addView(actionGrid(context, actions, profile.actionColumns))
        return panel
    }

    fun actionGrid(context: Context, actions: List<Action>, columns: Int = AdaptiveUiProfile.from(context).actionColumns): LinearLayout {
        val safeColumns = columns.coerceIn(1, 4)
        val grid = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        actions.chunked(safeColumns).forEach { group ->
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            group.forEachIndexed { index, action ->
                row.addView(actionTile(context, action), LinearLayout.LayoutParams(0, dp(context, if (safeColumns >= 4) 104 else 96), 1f).apply {
                    leftMargin = dp(context, if (index == 0) 0 else 4)
                    rightMargin = dp(context, if (index == group.lastIndex) 0 else 4)
                    bottomMargin = dp(context, 8)
                })
            }
            repeat(safeColumns - group.size) {
                row.addView(View(context), LinearLayout.LayoutParams(0, dp(context, 96), 1f).apply {
                    leftMargin = dp(context, 4)
                    rightMargin = dp(context, 4)
                })
            }
            grid.addView(row)
        }
        return grid
    }

    fun actionTile(context: Context, action: Action): View {
        val tile = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = ripple(context, SURFACE_RAISED, 0x3375E0C0)
            setPadding(dp(context, 6), dp(context, 7), dp(context, 6), dp(context, 6))
            minimumHeight = dp(context, 92)
            isClickable = true
            isFocusable = true
            contentDescription = action.title + ". " + action.subtitle
            setOnClickListener { action.onClick() }
        }
        tile.addView(TextView(context).apply {
            text = action.symbol
            textSize = 19f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(MINT)
            background = rounded(context, 0xFF243746.toInt(), 0xFF304A5A.toInt(), 11f)
        }, LinearLayout.LayoutParams(dp(context, 34), dp(context, 34)).apply { bottomMargin = dp(context, 5) })
        val copy = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        copy.addView(TextView(context).apply {
            text = action.title
            textSize = 12f
            maxLines = 2
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(TEXT)
        })
        copy.addView(TextView(context).apply {
            text = action.subtitle
            textSize = 10f
            maxLines = 1
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            setPadding(0, dp(context, 2), 0, 0)
        })
        tile.addView(copy, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        return tile
    }

    /** Generic elevated surface for reusable cards and feature widgets. */
    fun card(context: Context, content: View, raised: Boolean = false): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(context, if (raised) SURFACE_RAISED else SURFACE, OUTLINE, 18f)
            setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14))
            addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

    /** Reusable title/subtitle partial for cards, sheets and tool sections. */
    fun sectionHeader(context: Context, title: String, subtitle: String? = null): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply {
                text = title
                textSize = 16f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                setTextColor(TEXT)
            })
            if (!subtitle.isNullOrBlank()) addView(TextView(context).apply {
                text = subtitle
                textSize = 11f
                setTextColor(MUTED)
                setPadding(0, dp(context, 3), 0, 0)
            })
        }

    /** Compact state or category chip; minimum height supports touch exploration. */
    fun chip(context: Context, label: String, accent: Int = MINT): TextView =
        TextView(context).apply {
            text = label
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accent)
            gravity = Gravity.CENTER
            background = rounded(context, SURFACE_RAISED, OUTLINE, 20f)
            setPadding(dp(context, 11), dp(context, 7), dp(context, 11), dp(context, 7))
            minimumHeight = dp(context, 32)
        }

    /** Reusable metric/status widget with a strong value and descriptive label. */
    fun metricWidget(context: Context, value: String, label: String, accent: Int = CYAN): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(context, SURFACE_RAISED, OUTLINE, 16f)
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
            addView(TextView(context).apply {
                text = value
                textSize = 20f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                setTextColor(accent)
            })
            addView(TextView(context).apply {
                text = label
                textSize = 10f
                setTextColor(MUTED)
                maxLines = 2
            })
        }

    /** Empty-state partial for future project, asset and history collections. */
    fun emptyState(context: Context, title: String, detail: String): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(context, SURFACE, OUTLINE, 18f)
            setPadding(dp(context, 20), dp(context, 24), dp(context, 20), dp(context, 24))
            addView(TextView(context).apply {
                text = title
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(TEXT)
            })
            addView(TextView(context).apply {
                text = detail
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(MUTED)
                setPadding(0, dp(context, 6), 0, 0)
            })
        }

    /** Compact contextual command used by the persistent workspace toolbar. */
    fun toolbarButton(context: Context, label: String, onClick: () -> Unit): TextView =
        TextView(context).apply {
            text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(MINT)
            background = ripple(context, SURFACE_RAISED, 0x3375E0C0)
            setPadding(dp(context, 14), 0, dp(context, 14), 0)
            minWidth = dp(context, 64)
            minimumHeight = dp(context, 44)
            isClickable = true
            isFocusable = true
            contentDescription = label
            setOnClickListener { onClick() }
        }

    /** Workspace navigation chip with explicit selected-state styling. */
    fun navigationChip(context: Context, label: String, selected: Boolean, onClick: () -> Unit): TextView =
        TextView(context).apply {
            text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(context, 13), 0, dp(context, 13), 0)
            minimumHeight = dp(context, 38)
            minWidth = dp(context, 48)
            isClickable = true
            isFocusable = true
            contentDescription = label
            setNavigationChipSelected(this, selected)
            setOnClickListener { onClick() }
        }

    fun setNavigationChipSelected(view: TextView, selected: Boolean) {
        view.isSelected = selected
        view.setTextColor(if (selected) INK else MUTED)
        view.background = rounded(
            view.context,
            if (selected) MINT else SURFACE_RAISED,
            if (selected) MINT else OUTLINE,
            19f
        )
    }

    fun previewContainer(context: Context) = android.widget.FrameLayout(context).apply {
        background = rounded(context, SURFACE_RAISED, OUTLINE, 22f)
        clipToOutline = true
    }

    fun statusText(context: Context) = TextView(context).apply {
        textSize = 12f
        setTextColor(TEXT)
        background = rounded(context, SURFACE, OUTLINE, 14f)
        setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
    }

    fun spacer(context: Context, heightDp: Int = 10): View = space(context, heightDp)

    private fun space(context: Context, heightDp: Int): View =
        View(context).apply { layoutParams = LinearLayout.LayoutParams(1, dp(context, heightDp)) }

    private fun horizontalSpace(context: Context, widthDp: Int): View =
        View(context).apply { layoutParams = LinearLayout.LayoutParams(dp(context, widthDp), 1) }

    private fun rounded(context: Context, fill: Int, stroke: Int, radiusDp: Float) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(context, radiusDp)
        setStroke(dp(context, 1), stroke)
    }

    private fun ripple(context: Context, fill: Int, rippleColor: Int) =
        RippleDrawable(ColorStateList.valueOf(rippleColor), rounded(context, fill, OUTLINE, 14f), null)

    private fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
    private fun dp(context: Context, value: Float): Float = value * context.resources.displayMetrics.density
}
