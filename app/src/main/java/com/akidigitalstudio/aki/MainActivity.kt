package com.akidigitalstudio.aki

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import kotlin.math.max
import kotlin.math.sqrt

class MainActivity : Activity() {
    private lateinit var preview: ImageView
    private lateinit var status: TextView
    private var sourceUri: Uri? = null
    private var previewBitmap: Bitmap? = null
    private var rotation = 0f
    private var monochrome = false
    private var english = false
    private var cropRatio: Float? = null
    private var outputWidthPx: Int? = null
    private var outputHeightPx: Int? = null
    private var cropLabel: String? = null

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
        page.addView(preview, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(320)
        ).apply { topMargin = dp(18); bottomMargin = dp(10) })
        page.addView(status)

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(actionButton(if (english) "Open photo" else "ፎቶ ክፈት") { choosePhoto() }, weighted())
        row1.addView(actionButton(if (english) "Rotate" else "አዙር") { rotatePhoto() }, weighted())
        page.addView(row1)

        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(actionButton(if (english) "Crop" else "ቁረጥ") { chooseCrop() }, weighted())
        row2.addView(actionButton(if (english) "ID / Passport" else "መታወቂያ / ፓስፖርት") { chooseIdPreset() }, weighted())
        page.addView(row2)

        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(actionButton(if (english) "B&W" else "ጥቁር ነጭ") { toggleMonochrome() }, weighted())
        row3.addView(actionButton(if (english) "Reset" else "መልስ") { resetEdits() }, weighted())
        row3.addView(actionButton(if (english) "Save copy" else "ቅጂ አስቀምጥ") { saveCopy() }, weighted())
        page.addView(row3)

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
            previewBitmap = buildEditedBitmap(PREVIEW_MAX_EDGE)
            renderPreview()
            showStatus(if (english) "Photo opened locally. Original file is unchanged." else "ፎቶው በመሣሪያው ላይ ተከፍቷል። ዋናው ፋይል አልተቀየረም።")
        } catch (error: Exception) {
            sourceUri = null
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
                val cropped = ImageEditorEngine.cropCenter(image, ratio)
                image.recycle()
                image = cropped
            }
            if (monochrome) {
                val filtered = ImageEditorEngine.monochrome(image)
                image.recycle()
                image = filtered
            }
            val targetW = outputWidthPx
            val targetH = outputHeightPx
            if (targetW != null && targetH != null && maxEdge > PREVIEW_MAX_EDGE) {
                val resized = ImageEditorEngine.resize(image, targetW, targetH)
                image.recycle()
                image = resized
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
                outputWidthPx = null
                outputHeightPx = null
                cropLabel = if (which == 0) null else labels[which]
                refreshPreview()
            }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
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
        cropLabel = label
        refreshPreview()
    }

    private fun refreshPreview() {
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
        cropLabel = null
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

    private fun renderPreview() {
        preview.setImageBitmap(previewBitmap)
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
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_OPEN = 41
        private const val PREVIEW_MAX_EDGE = 1800
        private const val EXPORT_MAX_EDGE = 4096
    }
}
