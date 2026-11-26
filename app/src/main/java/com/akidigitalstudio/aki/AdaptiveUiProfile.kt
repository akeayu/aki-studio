package com.akidigitalstudio.aki

import android.content.Context
import android.content.res.Configuration

enum class WindowWidthClass { COMPACT, MEDIUM, EXPANDED }

/** Window-aware layout tokens derived from the current app window, not device marketing names. */
data class AdaptiveUiProfile(
    val widthDp: Int, val heightDp: Int, val smallestWidthDp: Int,
    val orientation: Int, val density: Float, val fontScale: Float,
    val widthClass: WindowWidthClass, val isLandscape: Boolean, val isTabletLike: Boolean,
    val actionColumns: Int, val panelColumns: Int, val previewHeightDp: Int,
    val pagePaddingDp: Int, val maxContentWidthDp: Int
) {
    companion object {
        fun from(context: Context): AdaptiveUiProfile {
            val config = context.resources.configuration
            val width = config.screenWidthDp.coerceAtLeast(320)
            val height = config.screenHeightDp.coerceAtLeast(320)
            val smallest = config.smallestScreenWidthDp
            val landscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
            val widthClass = classify(width)
            val baseColumns = when (widthClass) {
                WindowWidthClass.COMPACT -> 2
                WindowWidthClass.MEDIUM -> 3
                WindowWidthClass.EXPANDED -> 4
            }
            val columns = if (config.fontScale >= 1.35f) (baseColumns - 1).coerceAtLeast(1) else baseColumns
            val previewHeight = when (widthClass) {
                WindowWidthClass.COMPACT -> if (landscape) 190 else 270
                WindowWidthClass.MEDIUM -> if (landscape) 240 else 330
                WindowWidthClass.EXPANDED -> if (landscape) 300 else 390
            }
            return AdaptiveUiProfile(
                width, height, smallest, config.orientation, context.resources.displayMetrics.density,
                config.fontScale, widthClass, landscape, smallest >= 600, columns,
                if (width >= 840 && height >= 480 && config.fontScale < 1.5f) 2 else 1,
                previewHeight,
                when (widthClass) {
                    WindowWidthClass.COMPACT -> 14
                    WindowWidthClass.MEDIUM -> 22
                    WindowWidthClass.EXPANDED -> 30
                },
                1320
            )
        }

        fun classify(widthDp: Int): WindowWidthClass = when {
            widthDp >= 840 -> WindowWidthClass.EXPANDED
            widthDp >= 600 -> WindowWidthClass.MEDIUM
            else -> WindowWidthClass.COMPACT
        }
    }
}
