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

    fun page(context: Context) = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, 16), dp(context, 18), dp(context, 16), dp(context, 28))
        setBackgroundColor(INK)
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
        meta.addView(space(context, 8))
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

    fun panel(context: Context, title: String, subtitle: String, actions: List<Action>, english: Boolean): LinearLayout {
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
        panel.addView(actionGrid(context, actions))
        return panel
    }

    fun actionGrid(context: Context, actions: List<Action>): LinearLayout {
        val grid = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        actions.chunked(2).forEach { pair ->
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEachIndexed { index, action ->
                row.addView(actionTile(context, action), LinearLayout.LayoutParams(0, dp(context, 76), 1f).apply {
                    if (index == 0) rightMargin = dp(context, 6) else leftMargin = dp(context, 6)
                    bottomMargin = dp(context, 8)
                })
            }
            if (pair.size == 1) row.addView(View(context), LinearLayout.LayoutParams(0, dp(context, 76), 1f).apply {
                leftMargin = dp(context, 6)
            })
            grid.addView(row)
        }
        return grid
    }

    fun actionTile(context: Context, action: Action): View {
        val tile = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = ripple(context, SURFACE_RAISED, 0x3375E0C0)
            setPadding(dp(context, 10), dp(context, 8), dp(context, 8), dp(context, 8))
            isClickable = true
            isFocusable = true
            contentDescription = action.title + ". " + action.subtitle
            setOnClickListener { action.onClick() }
        }
        tile.addView(TextView(context).apply {
            text = action.symbol
            textSize = 21f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(MINT)
            background = rounded(context, 0xFF243746.toInt(), 0xFF304A5A.toInt(), 12f)
        }, LinearLayout.LayoutParams(dp(context, 42), dp(context, 42)).apply { rightMargin = dp(context, 9) })
        val copy = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        copy.addView(TextView(context).apply {
            text = action.title
            textSize = 12f
            maxLines = 2
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(TEXT)
        })
        copy.addView(TextView(context).apply {
            text = action.subtitle
            textSize = 9f
            maxLines = 1
            setTextColor(MUTED)
            setPadding(0, dp(context, 3), 0, 0)
        })
        tile.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return tile
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
