package com.akidigitalstudio.aki

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.FrameLayout
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.text.InputType
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import kotlin.math.max
import kotlin.math.sqrt

class MainActivity : Activity() {
    private lateinit var preview: ImageView
    private lateinit var guideOverlay: GuideOverlayView
    private lateinit var maskOverlay: ManualMaskView
    private var backgroundReplaceEnabled = false
    private var autoMaskBitmap: Bitmap? = null
    private var autoPreviewBitmap: Bitmap? = null
    private var backgroundColor = android.graphics.Color.WHITE
    private lateinit var status: TextView
    private var sourceUri: Uri? = null
    private var previewBitmap: Bitmap? = null
    private var originalPreviewBitmap: Bitmap? = null
    private var showingOriginal = false
    private var rotation = 0f
    private var monochrome = false
    private var english = false
    private var cropRatio: Float? = null
    private var outputWidthPx: Int? = null
    private var outputHeightPx: Int? = null
    private var resizeMaxWidthPx: Int? = null
    private var resizeMaxHeightPx: Int? = null
    private var cropLabel: String? = null
    private var cropHorizontal = 0.5f
    private var cropVertical = 0.5f
    private var guidesVisible = false
    private var headHeightPercent = 70
    private var eyeLinePercent = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        showStatus(if (english) "Ready. Your photos stay on this device." else "ዝግጁ ነው። ፎቶዎችዎ በዚህ መሣሪያ ላይ ብቻ ይቆያሉ።")
    }

    private fun buildScreen() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            setBackgroundColor(0xFFF5F4EF.toInt())
        }
        val heading = TextView(this).apply {
            text = "Aki Studio"
            textSize = 28f
            setTextColor(0xFF17382B.toInt())
            gravity = Gravity.CENTER_VERTICAL
        }
        val tagline = TextView(this).apply {
            text = "A New Way to Create"
            textSize = 14f
            setTextColor(0xFF52645A.toInt())
        }
        preview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFFE3E6E0.toInt())
            contentDescription = "Local photo preview"
        }
        status = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFF39483F.toInt())
            setPadding(0, dp(10), 0, dp(10))
        }
        page.addView(heading)
        page.addView(tagline)
        guideOverlay = GuideOverlayView(this)
        val previewFrame = FrameLayout(this).apply {
            addView(preview, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(guideOverlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            maskOverlay = ManualMaskView(this)
            addView(maskOverlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        page.addView(previewFrame, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(320)
        ).apply { topMargin = dp(18); bottomMargin = dp(10) })
        page.addView(status)

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(actionButton(if (english) "Open photo" else "ፎቶ ክፈት") { choosePhoto() }, weighted())
        row1.addView(actionButton(if (english) "Rotate" else "አዙር") { rotatePhoto() }, weighted())
        page.addView(row1)

        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(actionButton(if (english) "Crop" else "ቁረጥ") { chooseCrop() }, weighted())
        row2.addView(actionButton(if (english) "Resize" else "መጠን ቀይር") { chooseResize() }, weighted())
        row2.addView(actionButton(if (english) "ID / Passport" else "መታወቂያ / ፓስፖርት") { chooseIdPreset() }, weighted())
        page.addView(row2)

        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(actionButton(if (english) "B&W" else "ጥቁር ነጭ") { toggleMonochrome() }, weighted())
        row3.addView(actionButton(if (english) "Position" else "ቦታ") { chooseCropPosition() }, weighted())
        row3.addView(actionButton(if (english) "Reset" else "መልስ") { resetEdits() }, weighted())
        row3.addView(actionButton(if (english) "Save copy" else "ቅጂ አስቀምጥ") { saveCopy() }, weighted())
        page.addView(row3)
        val row4 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row4.addView(actionButton(if (english) "Photo guides" else "የፎቶ መመሪያ") { configurePhotoGuides() }, weighted())
        row4.addView(actionButton(if (english) "Print sheet PDF" else "የህትመት PDF") { configurePrintSheet() }, weighted())
        page.addView(row4)

        val row5 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row5.addView(actionButton(if (english) "Photo standard" else "የፎቶ መስፈርት") { configurePhotoSpecification() }, weighted())
        page.addView(row5)

        val row6 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row6.addView(actionButton(if (english) "Photo quality" else "የፎቶ ጥራት") { assessPhotoQuality() }, weighted())
        page.addView(row6)

        val row7 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row7.addView(actionButton(if (english) "Manual background mask" else "በእጅ የጀርባ ማስክ") { configureManualMask() }, weighted())
        page.addView(row7)

        val row8 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row8.addView(actionButton(if (english) "Mask controls" else "የማስክ መቆጣጠሪያ") { configureMaskControls() }, weighted())
        row8.addView(actionButton(if (english) "Before / After" else "ከዚያ በፊት / በኋላ") { toggleBeforeAfter() }, weighted())
        page.addView(row8)

        val row9 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row9.addView(actionButton(if (english) "Quick local background estimate" else "የጀርባ ቦታን በመሣሪያው ገምት") { runLocalForegroundEstimate() }, weighted())
        page.addView(row9)

        val language = Button(this).apply {
            text = if (english) "አማርኛ" else "English"
            setOnClickListener {
                english = !english
                buildScreen()
                renderPreview()
                showStatus(if (english) "Ready. Your photos stay on this device." else "ዝግጁ ነው። ፎቶዎችዎ በዚህ መሣሪያ ላይ ብቻ ይቆያሉ።")
            }
        }
        page.addView(language)
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun weighted() = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
        setMargins(dp(3), dp(4), dp(3), dp(4))
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_OPEN)
    }

    @Deprecated("Uses the platform activity result contract to avoid an external dependency.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_OPEN || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // Some document providers grant a transient read-only URI.
        }
        sourceUri = uri
        clearEdits()
        try {
            previewBitmap?.recycle()
            originalPreviewBitmap?.recycle()
            originalPreviewBitmap = decodeBounded(uri, ORIGINAL_PREVIEW_MAX_EDGE)
            showingOriginal = false
            previewBitmap = buildEditedBitmap(PREVIEW_MAX_EDGE)
            renderPreview()
            showStatus(if (english) "Photo opened locally. Original file is unchanged." else "ፎቶው በመሣሪያው ላይ ተከፍቷል። ዋናው ፋይል አልተቀየረም።")
        } catch (error: Exception) {
            sourceUri = null
            originalPreviewBitmap?.recycle(); originalPreviewBitmap = null
            showStatus(if (english) "Could not open this image: ${error.message ?: "unsupported file"}" else "ፎቶውን መክፈት አልተቻለም።")
        }
    }

    private fun decodeBounded(uri: Uri, maxEdge: Int): Bitmap {
        val source = ImageDecoder.createSource(contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val w = info.size.width
            val h = info.size.height
            val maxPixels = max(1L, Runtime.getRuntime().maxMemory() / 4L / 4L)
            val memoryScale = sqrt(maxPixels.toDouble() / (w.toLong() * h.toLong()).coerceAtLeast(1L))
            val edgeScale = maxEdge.toDouble() / max(w, h).toDouble()
            val scale = minOf(1.0, memoryScale, edgeScale)
            decoder.setTargetSize(max(1, (w * scale).toInt()), max(1, (h * scale).toInt()))
        }
    }

    private fun buildEditedBitmap(maxEdge: Int): Bitmap {
        val uri = sourceUri ?: throw IOException("No source image")
        var image = decodeBounded(uri, maxEdge)
        try {
            repeat((rotation / 90f).toInt()) {
                val rotated = ImageEditorEngine.rotate(image, 90f)
                image.recycle()
                image = rotated
            }
            cropRatio?.let { ratio ->
                val cropped = ImageEditorEngine.cropCenter(image, ratio, cropHorizontal, cropVertical)
                image.recycle()
                image = cropped
            }
            if (backgroundReplaceEnabled) {
                val mask = autoMaskBitmap?.let {
                    if (it.width == image.width && it.height == image.height) it
                    else Bitmap.createScaledBitmap(it, image.width, image.height, true)
                } ?: maskOverlay.renderMask(image.width, image.height)
                val ownsMask = mask !== autoMaskBitmap
                val replaced = try { BackgroundReplacementEngine.replaceSolid(image, mask, backgroundColor) } finally {
                    if (ownsMask && !mask.isRecycled) mask.recycle()
                }
                image.recycle()
                image = replaced
            }
            if (monochrome) {
                val filtered = ImageEditorEngine.monochrome(image)
                image.recycle()
                image = filtered
            }
            val targetW = outputWidthPx
            val targetH = outputHeightPx
            if (maxEdge > PREVIEW_MAX_EDGE) {
                val customW = resizeMaxWidthPx
                val customH = resizeMaxHeightPx
                val resized = when {
                    customW != null && customH != null ->
                        ImageEditorEngine.resizeWithin(image, customW, customH)
                    targetW != null && targetH != null ->
                        ImageEditorEngine.resize(image, targetW, targetH)
                    else -> null
                }
                if (resized != null) {
                    image.recycle()
                    image = resized
                }
            }
            return image
        } catch (error: Exception) {
            image.recycle()
            throw error
        }
    }

    private fun chooseCrop() {
        if (sourceUri == null) return noPhoto()
        val labels = arrayOf(
            if (english) "Original proportions" else "የመጀመሪያው መጠን",
            "1:1  Square", "3:4  Portrait", "4:5  Portrait",
            "5:4  Landscape", "16:9  Landscape"
        )
        val ratios = arrayOf<Float?>(null, 1f, 3f / 4f, 4f / 5f, 5f / 4f, 16f / 9f)
        AlertDialog.Builder(this)
            .setTitle(if (english) "Center crop" else "መሃል ላይ መቁረጥ")
            .setItems(labels) { _, which ->
                cropRatio = ratios[which]
                if (which == 0) { cropHorizontal = 0.5f; cropVertical = 0.5f }
                outputWidthPx = null
                outputHeightPx = null
                resizeMaxWidthPx = null
                resizeMaxHeightPx = null
                cropLabel = if (which == 0) null else labels[which]
                refreshPreview()
            }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
    }

    private fun chooseCropPosition() {
        if (sourceUri == null) return noPhoto()
        if (cropRatio == null) {
            showStatus(if (english) "Choose a crop ratio first." else "መጀመሪያ የቁረጥ መጠን ይምረጡ።")
            return
        }
        val positions = arrayOf(
            if (english) "Top left" else "ላይ ግራ",
            if (english) "Top center" else "ላይ መሃል",
            if (english) "Top right" else "ላይ ቀኝ",
            if (english) "Middle left" else "መሃል ግራ",
            if (english) "Center" else "መሃል",
            if (english) "Middle right" else "መሃል ቀኝ",
            if (english) "Bottom left" else "ታች ግራ",
            if (english) "Bottom center" else "ታች መሃል",
            if (english) "Bottom right" else "ታች ቀኝ"
        )
        val alignments = arrayOf(
            0f to 0f, 0.5f to 0f, 1f to 0f,
            0f to 0.5f, 0.5f to 0.5f, 1f to 0.5f,
            0f to 1f, 0.5f to 1f, 1f to 1f
        )
        AlertDialog.Builder(this)
            .setTitle(if (english) "Crop position" else "የቁረጥ ቦታ")
            .setItems(positions) { _, which ->
                cropHorizontal = alignments[which].first
                cropVertical = alignments[which].second
                refreshPreview()
            }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
    }

    private fun chooseResize() {
        if (sourceUri == null) return noPhoto()
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val widthInput = EditText(this).apply {
            hint = if (english) "Maximum width (px)" else "ከፍተኛ ስፋት (px)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText((resizeMaxWidthPx ?: 1600).toString())
        }
        val heightInput = EditText(this).apply {
            hint = if (english) "Maximum height (px)" else "ከፍተኛ ቁመት (px)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText((resizeMaxHeightPx ?: 1600).toString())
        }
        fields.addView(widthInput)
        fields.addView(heightInput)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (english) "Resize within bounds" else "የፎቶ መጠን ቀይር")
            .setMessage(if (english) "The image keeps its current aspect ratio and fits inside these pixel limits." else "የፎቶው ርዝመትና ስፋት ግንኙነት ሳይቀየር በተሰጡት የፒክስል ገደቦች ውስጥ ይገባል።")
            .setView(fields)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Apply" else "ተግብር", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val width = widthInput.text.toString().toIntOrNull()
                val height = heightInput.text.toString().toIntOrNull()
                if (width == null || height == null || width !in 1..4096 || height !in 1..4096 ||
                    width.toLong() * height.toLong() > 4_000_000L) {
                    showStatus(if (english) "Enter valid pixel limits (1–4096; maximum 4 million pixels)." else "ትክክለኛ የፒክስል መጠን ያስገቡ (1–4096፤ ከ4 ሚሊዮን ፒክስል አይበልጥም)።")
                    return@setOnClickListener
                }
                resizeMaxWidthPx = width
                resizeMaxHeightPx = height
                outputWidthPx = null
                outputHeightPx = null
                cropLabel = "Resize ≤ ${width}×${height} px"
                dialog.dismiss()
                refreshPreview()
            }
        }
        dialog.show()
    }

    private fun chooseIdPreset() {
        if (sourceUri == null) return noPhoto()
        val labels = arrayOf(
            "3.5 × 4.5 cm — 35 × 45 mm (413 × 531 px @ 300 DPI)",
            "2 × 2 inch — US passport (600 × 600 px @ 300 DPI)",
            if (english) "Clear crop preset" else "የቁረጥ መጠንን አጽዳ"
        )
        AlertDialog.Builder(this)
            .setTitle(if (english) "ID and passport formats" else "የመታወቂያ እና ፓስፖርት መጠኖች")
            .setItems(labels) { _, which ->
                when (which) {
                    0 -> applyPreset(35f / 45f, 413, 531, labels[0])
                    1 -> applyPreset(1f, 600, 600, labels[1])
                    else -> {
                        cropRatio = null
                        outputWidthPx = null
                        outputHeightPx = null
                        resizeMaxWidthPx = null
                        resizeMaxHeightPx = null
                        cropLabel = null
                        refreshPreview()
                    }
                }
            }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
    }

    private fun applyPreset(ratio: Float, width: Int, height: Int, label: String) {
        cropRatio = ratio
        outputWidthPx = width
        outputHeightPx = height
        resizeMaxWidthPx = null
        resizeMaxHeightPx = null
        cropLabel = label
        refreshPreview()
    }

    private fun refreshPreview() {
        // Geometry edits invalidate a mask estimated from the previous preview.
        if (autoMaskBitmap != null) {
            autoMaskBitmap?.recycle(); autoMaskBitmap = null
            autoPreviewBitmap?.recycle(); autoPreviewBitmap = null
            backgroundReplaceEnabled = false
        }
        try {
            val next = buildEditedBitmap(PREVIEW_MAX_EDGE)
            previewBitmap?.recycle()
            previewBitmap = next
            renderPreview()
            val description = cropLabel?.let { " · $it" } ?: ""
            showStatus(if (english) "Local edit preview$description. Original is unchanged." else "በመሣሪያው ላይ የተደረገ ማስተካከያ$description። ዋናው ፎቶ አልተቀየረም።")
        } catch (error: Exception) {
            showStatus(if (english) "Could not update preview: ${error.message ?: "image error"}" else "ቅድመ እይታውን ማዘመን አልተቻለም።")
        }
    }

    private fun clearEdits() {
        rotation = 0f
        monochrome = false
        cropRatio = null
        outputWidthPx = null
        outputHeightPx = null
        resizeMaxWidthPx = null
        resizeMaxHeightPx = null
        cropLabel = null
        cropHorizontal = 0.5f
        cropVertical = 0.5f
        backgroundReplaceEnabled = false
        autoMaskBitmap?.recycle(); autoMaskBitmap = null
        autoPreviewBitmap?.recycle(); autoPreviewBitmap = null
        if (::maskOverlay.isInitialized) { maskOverlay.clearMask(); maskOverlay.configure(previewBitmap, false) }
    }

    private fun rotatePhoto() {
        if (sourceUri == null) return noPhoto()
        rotation = (rotation + 90f) % 360f
        refreshPreview()
    }

    private fun toggleMonochrome() {
        if (sourceUri == null) return noPhoto()
        monochrome = !monochrome
        refreshPreview()
    }

    private fun resetEdits() {
        if (sourceUri == null) return noPhoto()
        clearEdits()
        refreshPreview()
        showStatus(if (english) "Edits reset. Original is unchanged." else "ለውጦቹ ተመልሰዋል። ዋናው ፎቶ አልተቀየረም።")
    }

    private fun saveCopy() {
        if (sourceUri == null) return noPhoto()
        val dimensions = (previewBitmap?.width ?: 0).toString() + " × " + (previewBitmap?.height ?: 0) + " px"
        AlertDialog.Builder(this)
            .setTitle(if (english) "Confirm export" else "የፎቶ ማስቀመጥን ያረጋግጡ")
            .setMessage(if (english) "Save a new JPEG copy ($dimensions) to Pictures/Aki Studio? The original file will not be changed." else "አዲስ JPEG ቅጂ ($dimensions) በPictures/Aki Studio ይቀመጥ? ዋናው ፋይል አይቀየርም።")
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Save copy" else "ቅጂ አስቀምጥ") { _, _ -> performSaveCopy() }
            .show()
    }

    private fun performSaveCopy() {
        if (sourceUri == null) return noPhoto()
        try {
            val output = buildEditedBitmap(EXPORT_MAX_EDGE)
            try {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "aki_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Aki Studio")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val destination = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Could not create output file")
                try {
                    contentResolver.openOutputStream(destination)?.use { stream ->
                        if (!output.compress(Bitmap.CompressFormat.JPEG, 94, stream)) {
                            throw IOException("JPEG encoding failed")
                        }
                    } ?: throw IOException("Could not write output")
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(destination, values, null, null)
                    showStatus(if (english) "Saved a new copy in Pictures/Aki Studio." else "አዲስ ቅጂ በPictures/Aki Studio ተቀምጧል።")
                } catch (error: Exception) {
                    contentResolver.delete(destination, null, null)
                    throw error
                }
            } finally {
                output.recycle()
            }
        } catch (error: Exception) {
            showStatus(if (english) "Save failed: ${error.message ?: "unknown error"}" else "ማስቀመጥ አልተሳካም።")
        }
    }


    private fun toggleBeforeAfter() {
        if (sourceUri == null) return noPhoto()
        showingOriginal = !showingOriginal
        preview.setImageBitmap(if (showingOriginal) originalPreviewBitmap else (autoPreviewBitmap ?: previewBitmap))
        guideOverlay.configure(if (showingOriginal) originalPreviewBitmap else previewBitmap, headHeightPercent, eyeLinePercent, guidesVisible && !showingOriginal)
        maskOverlay.configure(previewBitmap, false)
        showStatus(if (english) {
            if (showingOriginal) "Before: original image preview. No edits are applied to the source." else "After: current edited preview."
        } else {
            if (showingOriginal) "ከማስተካከያ በፊት፦ ዋናው ፎቶ።" else "ከማስተካከያ በኋላ፦ አሁን ያለው ቅድመ እይታ።"
        })
    }

    private fun configureMaskControls() {
        if (sourceUri == null) return noPhoto()
        val options = arrayOf(
            if (english) "Undo last stroke" else "የመጨረሻውን ስትሮክ መልስ",
            if (english) "Redo stroke" else "ስትሮክ እንደገና አድርግ",
            if (english) "Brush size and edge softness" else "የብሩሽ መጠንና የጠርዝ ለስላሳነት"
        )
        AlertDialog.Builder(this).setTitle(if (english) "Mask editing controls" else "የማስክ ማስተካከያ")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { maskOverlay.undo(); showStatus(if (english) "Undo applied." else "የመጨረሻው ስትሮክ ተመልሷል።") }
                    1 -> { maskOverlay.redo(); showStatus(if (english) "Redo applied." else "ስትሮኩ እንደገና ተተግብሯል።") }
                    2 -> {
                        val fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
                        val brush = EditText(this).apply { hint = if (english) "Brush width (1–12%)" else "የብሩሽ ስፋት (1–12%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText("4") }
                        val feather = EditText(this).apply { hint = if (english) "Edge softness (0–4%)" else "የጠርዝ ለስላሳነት (0–4%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText("1") }
                        fields.addView(brush); fields.addView(feather)
                        AlertDialog.Builder(this).setTitle(if (english) "Brush and feather" else "ብሩሽና ጠርዝ")
                            .setView(fields).setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
                            .setPositiveButton(if (english) "Apply" else "ተግብር") { _, _ ->
                                val b = brush.text.toString().toFloatOrNull()
                                val f = feather.text.toString().toFloatOrNull()
                                if (b == null || f == null || b !in 1f..12f || f !in 0f..4f) {
                                    showStatus(if (english) "Brush must be 1–12; feather must be 0–4." else "ብሩሽ 1–12፣ ጠርዝ 0–4 መሆን አለበት።")
                                } else {
                                    maskOverlay.setBrushPercent(b); maskOverlay.setFeatherPercent(f)
                                    showStatus(if (english) "Brush settings apply to new strokes." else "የብሩሽ ቅንብሮች ለአዲስ ስትሮኮች ይሠራሉ።")
                                }
                            }.show()
                    }
                }
            }.show()
    }

    private fun runLocalForegroundEstimate() {
        if (sourceUri == null || previewBitmap == null) return noPhoto()
        try {
            val result = LocalForegroundSegmentationEngine().segment(previewBitmap!!)
            autoMaskBitmap?.recycle()
            autoPreviewBitmap?.recycle()
            autoMaskBitmap = result.foregroundMask
            backgroundReplaceEnabled = true
            maskOverlay.configure(previewBitmap, false)
            autoPreviewBitmap = BackgroundReplacementEngine.replaceSolid(previewBitmap!!, result.foregroundMask, backgroundColor)
            renderPreview()
            val caveat = if (english) {
                "Local estimate: " + result.method + ". Confidence is low (" + (result.confidence * 100).toInt() + "%). Border-colour heuristic only; inspect and correct the mask manually. Similar-coloured hair/clothing may be removed."
            } else {
                "በመሣሪያው ላይ የተገመተ፦ " + result.method + "። እምነት ዝቅተኛ ነው። የጠርዝ ቀለም ግምት ብቻ ነው፤ ማስኩን ይመርምሩና በእጅ ያስተካክሉ።"
            }
            showStatus(caveat)
        } catch (error: Exception) {
            showStatus(if (english) "Local foreground estimate failed: " + (error.message ?: "image error") else "የፊት ክፍል ግምት አልተሳካም።")
        }
    }

    private fun configureManualMask() {
        if (sourceUri == null) return noPhoto()
        val options = arrayOf(
            if (english) "Paint foreground protection" else "የፊት ክፍሉን ጠብቅ",
            if (english) "Erase mask strokes" else "የማስክ ስትሮክ አጥፋ",
            if (english) "Clear mask" else "ማስክ አጽዳ",
            if (english) "Choose solid background" else "ጠፍጣፋ የጀርባ ቀለም ምረጥ",
            if (english) "Disable replacement" else "የጀርባ ለውጥ አቁም"
        )
        AlertDialog.Builder(this)
            .setTitle(if (english) "Manual background editing" else "በእጅ የጀርባ ማስተካከያ")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (autoMaskBitmap != null) {
                            maskOverlay.setBaseMask(autoMaskBitmap)
                            autoMaskBitmap?.recycle(); autoMaskBitmap = null
                            autoPreviewBitmap?.recycle(); autoPreviewBitmap = null
                        }
                        backgroundReplaceEnabled = true
                        preview.setImageBitmap(previewBitmap)
                        maskOverlay.configure(previewBitmap, true, false)
                        showStatus(if (english) "Paint over the subject to protect it. Unpainted pixels will be replaced. Tap this tool again to finish." else "ሰውየውን ለመጠበቅ በላዩ ላይ ይሳሉ። ያልተሳሉ ፒክስሎች ይተካሉ።")
                        backgroundReplaceEnabled = true
                    }
                    1 -> {
                        maskOverlay.configure(previewBitmap, true, true)
                        showStatus(if (english) "Erase unwanted mask strokes." else "የማይፈለጉ የማስክ ስትሮኮችን ያጥፉ።")
                    }
                    2 -> {
                        maskOverlay.clearMask()
                        backgroundReplaceEnabled = false
                        maskOverlay.configure(previewBitmap, false)
                        refreshPreview()
                    }
                    3 -> {
                        val labels = arrayOf("White", "Light gray", "Blue", "Green")
                        val colors = intArrayOf(0xFFFFFFFF.toInt(),0xFFECEFF1.toInt(),0xFFB3D9F2.toInt(),0xFFB7D7B0.toInt())
                        AlertDialog.Builder(this)
                            .setTitle(if (english) "Background color" else "የጀርባ ቀለም")
                            .setItems(labels) { _, colorIndex ->
                                backgroundColor = colors[colorIndex]
                                backgroundReplaceEnabled = true
                                maskOverlay.configure(previewBitmap, false)
                                refreshPreview()
                            }.show()
                    }
                    4 -> {
                        backgroundReplaceEnabled = false
                        maskOverlay.configure(previewBitmap, false)
                        refreshPreview()
                    }
                }
            }
            .setNegativeButton(if (english) "Close" else "ዝጋ", null)
            .show()
    }

    private fun assessPhotoQuality() {
        if (sourceUri == null) return noPhoto()
        var assessmentBitmap: Bitmap? = null
        try {
            assessmentBitmap = buildEditedBitmap(PREVIEW_MAX_EDGE)
            val report = PhotoQualityAssessment.assess(assessmentBitmap)
            val lines = mutableListOf<String>()
            lines += if (english) "Image: ${report.width} × ${report.height} px" else "ምስል፦ ${report.width} × ${report.height} px"
            lines += if (english) "Aspect ratio: ${"%.3f".format(report.aspectRatio)}" else "የጎን ጥምርታ፦ ${"%.3f".format(report.aspectRatio)}"
            lines += if (english) "Sharpness indicator: ${"%.1f".format(report.sharpnessScore)}" else "ጥርትነት አመልካች፦ ${"%.1f".format(report.sharpnessScore)}"
            lines += if (english) "Clipped shadows/highlights: ${"%.1f".format(report.clippedPercent)}%" else "የጠቆረ/የበራ የተቆረጠ ክፍል፦ ${"%.1f".format(report.clippedPercent)}%"
            lines += if (english) "Contrast (luma SD): ${"%.1f".format(report.contrastScore)}" else "ንፅፅር (luma SD)፦ ${"%.1f".format(report.contrastScore)}"
            lines += if (english) "Border uniformity indicator: ${"%.1f".format(report.borderVariation)}" else "የዳርቻ ተመሳሳይነት አመልካች፦ ${"%.1f".format(report.borderVariation)}"
            lines += ""
            lines += if (english) "Warnings" else "ማስጠንቀቂያዎች"
            lines += if (report.warnings.isEmpty()) {
                listOf(if (english) "No heuristic warnings. This is not a compliance approval." else "በግምታዊ መለኪያዎቹ ማስጠንቀቂያ አልተገኘም። ይህ የመስፈርት ማረጋገጫ አይደለም።")
            } else report.warnings.map { if (english) it else it }
            lines += ""
            lines += if (english) "These are on-device heuristic indicators only. Blur, exposure and border uniformity depend on image content; no face detection, background segmentation or official-standard verification is performed." else "እነዚህ በመሣሪያው ላይ የሚሰሩ ግምታዊ አመልካቾች ብቻ ናቸው። የፊት ለይቶ ማወቅ፣ የጀርባ ክፍል መለየት ወይም ይፋዊ መስፈርት ማረጋገጥ አይከናወንም።"
            AlertDialog.Builder(this)
                .setTitle(if (english) "Local photo quality assessment" else "በመሣሪያው ላይ የፎቶ ጥራት ግምገማ")
                .setMessage(lines.joinToString("\n"))
                .setPositiveButton(if (english) "Close" else "ዝጋ", null)
                .show()
        } catch (error: Exception) {
            showStatus(if (english) "Quality assessment failed: ${error.message ?: "image error"}" else "የጥራት ግምገማው አልተሳካም።")
        } finally {
            assessmentBitmap?.recycle()
        }
    }

    private fun configurePhotoSpecification() {
        if (sourceUri == null) return noPhoto()
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val nameInput = EditText(this).apply {
            hint = if (english) "Template name (e.g. personal 35 × 45 mm)" else "የመመሪያ ስም (ለምሳሌ 35 × 45 ሚሜ)"
        }
        val widthInput = EditText(this).apply {
            hint = if (english) "Photo width (mm)" else "የፎቶ ስፋት (ሚሜ)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val heightInput = EditText(this).apply {
            hint = if (english) "Photo height (mm)" else "የፎቶ ቁመት (ሚሜ)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val headInput = EditText(this).apply {
            hint = if (english) "Head height (% of photo)" else "የጭንቅላት ቁመት (%)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(headHeightPercent.toString())
        }
        val eyeInput = EditText(this).apply {
            hint = if (english) "Eye-line from top (%)" else "የዓይን መስመር (%)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(eyeLinePercent.toString())
        }
        fields.addView(TextView(this).apply {
            text = if (english)
                "Create a user-defined guide only. No issuing authority or verified official standard is supplied. Values are visual aids, not compliance certification."
            else
                "ይህ በተጠቃሚ የሚዘጋጅ መመሪያ ብቻ ነው። የሰጪ ባለሥልጣን ወይም የተረጋገጠ ይፋዊ መስፈርት አልተገለጸም። የሚታዩት መመሪያዎች የማረጋገጫ ማስረጃ አይደሉም።"
            textSize = 13f
        })
        listOf(nameInput, widthInput, heightInput, headInput, eyeInput).forEach { fields.addView(it) }
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (english) "User-defined photo template" else "በተጠቃሚ የሚዘጋጅ የፎቶ መመሪያ")
            .setView(fields)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Apply guide" else "መመሪያውን ተግብር", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString().trim()
                val widthMm = widthInput.text.toString().toIntOrNull()
                val heightMm = heightInput.text.toString().toIntOrNull()
                val head = headInput.text.toString().toIntOrNull()
                val eye = eyeInput.text.toString().toIntOrNull()
                if (name.isBlank() || name.length > 60 || widthMm == null || heightMm == null ||
                    widthMm !in 10..100 || heightMm !in 10..150 || head == null || head !in 40..90 ||
                    eye == null || eye !in 20..70) {
                    showStatus(if (english) "Enter a name, width 10–100 mm, height 10–150 mm, head 40–90%, and eye line 20–70%." else "ስም፣ ስፋት 10–100 ሚሜ፣ ቁመት 10–150 ሚሜ፣ ጭንቅላት 40–90% እና የዓይን መስመር 20–70% ያስገቡ።")
                    return@setOnClickListener
                }
                val spec = OfficialPhotoSpecEngine.PhotoSpec(
                    id = "user-" + name.hashCode().toUInt().toString(16),
                    displayName = name,
                    widthMm = widthMm,
                    heightMm = heightMm,
                    headHeightPercent = head,
                    eyeLineFromTopPercent = eye,
                    provenance = OfficialPhotoSpecEngine.Provenance.USER_DEFINED
                )
                val validation = OfficialPhotoSpecEngine.validate(spec)
                if (!validation.valid || validation.officialComplianceMayBeClaimed) {
                    showStatus(if (english) "Template validation failed. User-defined templates cannot claim official compliance." else "የመመሪያው ማረጋገጫ አልተሳካም። በተጠቃሚ የተዘጋጀ መመሪያ ይፋዊ መስፈርት ነው ማለት አይቻልም።")
                    return@setOnClickListener
                }
                val ratio = spec.widthMm.toFloat() / spec.heightMm.toFloat()
                val widthPx = OfficialPhotoSpecEngine.pixelsAt300Dpi(spec.widthMm)
                val heightPx = OfficialPhotoSpecEngine.pixelsAt300Dpi(spec.heightMm)
                if (widthPx.toLong() * heightPx.toLong() > 4_000_000L) {
                    showStatus(if (english) "Template output exceeds the safe pixel limit." else "የመመሪያው የፒክስል ውጤት የደህንነት ገደብን አልፏል።")
                    return@setOnClickListener
                }
                applyPreset(ratio, widthPx, heightPx, "$name — USER-DEFINED / UNVERIFIED")
                headHeightPercent = head
                eyeLinePercent = eye
                guidesVisible = true
                guideOverlay.configure(previewBitmap, head, eye, true)
                dialog.dismiss()
                showStatus(if (english)
                    "User-defined guide applied at nominal 300-DPI pixel dimensions. Not an official or verified standard."
                else
                    "በተጠቃሚ የተዘጋጀው መመሪያ ተግብሯል። የ300 DPI የፒክስል ስሌት ብቻ ነው፤ ይፋዊ ወይም የተረጋገጠ መስፈርት አይደለም።")
            }
        }
        dialog.show()
    }

    private fun configurePhotoGuides() {
        if (sourceUri == null) return noPhoto()
        val fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val headInput = EditText(this).apply { hint = if (english) "Head height (% of photo)" else "የጭንቅላት ቁመት (% ከፎቶው)"; inputType = InputType.TYPE_CLASS_NUMBER; setText(headHeightPercent.toString()) }
        val eyeInput = EditText(this).apply { hint = if (english) "Eye-line from top (%)" else "የዓይን መስመር ከላይ (%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText(eyeLinePercent.toString()) }
        fields.addView(TextView(this).apply {
            text = if (english) "Position the subject manually: fit the head inside the oval and align the eyes with the horizontal line. No face detection is performed." else "ሰውየውን በእጅ ያስተካክሉ፦ ጭንቅላቱን በኦቫሉ ውስጥ፣ ዓይኖቹንም ከአግድም መስመሩ ጋር ያስማሙ። የፊት ለይቶ ማወቅ አይከናወንም።"
            textSize = 13f
        })
        fields.addView(headInput); fields.addView(eyeInput)
        val dialog = AlertDialog.Builder(this).setTitle(if (english) "Head and eye-line guides" else "የጭንቅላትና የዓይን መመሪያ").setView(fields)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null).setPositiveButton(if (english) "Show guides" else "መመሪያዎችን አሳይ", null)
            .setNeutralButton(if (english) "Hide" else "ደብቅ", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val head = headInput.text.toString().toIntOrNull()
                val eye = eyeInput.text.toString().toIntOrNull()
                if (head == null || eye == null || head !in 40..90 || eye !in 20..70) {
                    showStatus(if (english) "Head height must be 40–90%; eye line must be 20–70%." else "የጭንቅላት ቁመት 40–90%፣ የዓይን መስመር 20–70% መሆን አለበት።")
                    return@setOnClickListener
                }
                headHeightPercent = head; eyeLinePercent = eye; guidesVisible = true
                guideOverlay.configure(previewBitmap, head, eye, true)
                dialog.dismiss()
                showStatus(if (english) "Guides shown. Adjust the subject manually; these are framing aids, not official compliance checks." else "መመሪያዎች ታይተዋል። ሰውየውን በእጅ ያስተካክሉ፤ እነዚህ የፍሬም እገዛዎች ናቸው እንጂ የመንግሥት መስፈርት ማረጋገጫ አይደሉም።")
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                guidesVisible = false
                guideOverlay.configure(previewBitmap, headHeightPercent, eyeLinePercent, false)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun configurePrintSheet() {
        if (sourceUri == null) return noPhoto()
        val fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val columnsInput = EditText(this).apply { hint = if (english) "Columns (1–5)" else "አምዶች (1–5)"; inputType = InputType.TYPE_CLASS_NUMBER; setText("3") }
        val rowsInput = EditText(this).apply { hint = if (english) "Rows (1–7)" else "ረድፎች (1–7)"; inputType = InputType.TYPE_CLASS_NUMBER; setText("4") }
        fields.addView(TextView(this).apply {
            text = if (english) "Create a single A4 portrait PDF page. The current edited photo repeats in equal cells with white margins and cut guides." else "አንድ A4 ቁመታዊ PDF ገጽ ይፍጠሩ። አሁን ያለው የተስተካከለ ፎቶ በእኩል ሳጥኖች ይደገማል።"
            textSize = 13f
        })
        fields.addView(columnsInput); fields.addView(rowsInput)
        val dialog = AlertDialog.Builder(this).setTitle(if (english) "A4 print sheet" else "A4 የህትመት ወረቀት").setView(fields)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null).setPositiveButton(if (english) "Create PDF" else "PDF ፍጠር", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val columns = columnsInput.text.toString().toIntOrNull()
                val rows = rowsInput.text.toString().toIntOrNull()
                if (columns == null || rows == null || columns !in 1..5 || rows !in 1..7) {
                    showStatus(if (english) "Choose 1–5 columns and 1–7 rows." else "1–5 አምድ እና 1–7 ረድፍ ይምረጡ።")
                    return@setOnClickListener
                }
                dialog.dismiss()
                exportPrintSheet(columns, rows)
            }
        }
        dialog.show()
    }

    private fun exportPrintSheet(columns: Int, rows: Int) {
        var photo: Bitmap? = null
        val document = PdfDocument()
        try {
            photo = buildEditedBitmap(EXPORT_MAX_EDGE)
            val info = PdfDocument.PageInfo.Builder(A4_WIDTH_PT, A4_HEIGHT_PT, 1).create()
            val page = document.startPage(info)
            val canvas = page.canvas
            canvas.drawColor(android.graphics.Color.WHITE)
            val margin = 28f
            val gap = 12f
            val cellWidth = (A4_WIDTH_PT - 2f * margin - (columns - 1) * gap) / columns
            val cellHeight = (A4_HEIGHT_PT - 2f * margin - (rows - 1) * gap) / rows
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF777777.toInt(); style = Paint.Style.STROKE; strokeWidth = 0.45f }
            val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF333333.toInt(); strokeWidth = 0.55f }
            for (r in 0 until rows) for (c in 0 until columns) {
                val left = margin + c * (cellWidth + gap)
                val top = margin + r * (cellHeight + gap)
                val cell = RectF(left, top, left + cellWidth, top + cellHeight)
                val scale = minOf(cell.width() / photo.width, cell.height() / photo.height)
                val w = photo.width * scale
                val h = photo.height * scale
                val dst = RectF(cell.centerX() - w / 2f, cell.centerY() - h / 2f, cell.centerX() + w / 2f, cell.centerY() + h / 2f)
                canvas.drawBitmap(photo, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
                canvas.drawRect(dst, border)
                val t = 4f
                canvas.drawLine(dst.left - t, dst.top, dst.left + t, dst.top, mark)
                canvas.drawLine(dst.right - t, dst.bottom, dst.right + t, dst.bottom, mark)
            }
            document.finishPage(page)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "aki_print_${System.currentTimeMillis()}.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Aki Studio")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: throw IOException("Could not create PDF destination")
            try {
                contentResolver.openOutputStream(uri)?.use { document.writeTo(it) } ?: throw IOException("Could not open PDF output")
                values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
                showStatus(if (english) "A4 print sheet saved in Documents/Aki Studio (${columns} × ${rows})." else "A4 የህትመት ወረቀት በDocuments/Aki Studio ተቀምጧል (${columns} × ${rows})።")
            } catch (error: Exception) {
                contentResolver.delete(uri, null, null); throw error
            }
        } catch (error: Exception) {
            showStatus(if (english) "PDF creation failed: ${error.message ?: "unknown error"}" else "PDF መፍጠር አልተሳካም።")
        } finally {
            photo?.recycle(); document.close()
        }
    }

    private fun renderPreview() {
        preview.setImageBitmap(if (showingOriginal) originalPreviewBitmap else previewBitmap)
        guideOverlay.configure(if (showingOriginal) originalPreviewBitmap else previewBitmap, headHeightPercent, eyeLinePercent, guidesVisible && !showingOriginal)
        if (::maskOverlay.isInitialized) maskOverlay.configure(previewBitmap, false)
    }

    private fun noPhoto() {
        showStatus(if (english) "Open a photo first." else "መጀመሪያ ፎቶ ይክፈቱ።")
    }

    private fun showStatus(message: String) {
        if (::status.isInitialized) status.text = message
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        previewBitmap?.recycle()
        previewBitmap = null
        originalPreviewBitmap?.recycle()
        originalPreviewBitmap = null
        autoMaskBitmap?.recycle(); autoMaskBitmap = null
        autoPreviewBitmap?.recycle(); autoPreviewBitmap = null
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_OPEN = 41
        private const val PREVIEW_MAX_EDGE = 1800
        private const val ORIGINAL_PREVIEW_MAX_EDGE = 1200
        private const val EXPORT_MAX_EDGE = 4096
        private const val A4_WIDTH_PT = 595
        private const val A4_HEIGHT_PT = 842
    }
}
