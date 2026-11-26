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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.text.InputType
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
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
    private var brightness = 0
    private var contrast = 0
    private var saturation = 100
    private var warmth = 0
    private var english = false
    private var cropRatio: Float? = null
    private var cropRectNormalized: FloatArray? = null
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
    private val segmentationExecutor: ExecutorService = Executors.newSingleThreadExecutor { task ->
        Thread(task, "aki-local-segmentation").apply { isDaemon = true }
    }
    private var segmentationFuture: Future<*>? = null
    private var segmentationRequestId = 0
    private val editHistory = EditHistory()
    private val designLayers = mutableListOf<DesignLayer>()
    private var suppressHistoryRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        showStatus(if (english) "Ready. Your photos stay on this device." else "ዝግጁ ነው። ፎቶዎችዎ በዚህ መሣሪያ ላይ ብቻ ይቆያሉ።")
    }

    private fun buildScreen() {
        window.statusBarColor = AkiStudioUi.INK
        window.navigationBarColor = AkiStudioUi.INK
        val page = AkiStudioUi.page(this)
        page.addView(AkiStudioUi.header(this, english) {
            english = !english
            buildScreen()
            renderPreview()
            showStatus(if (english) "Ready. Your photos stay on this device." else "ዝግጁ ነው። ፎቶዎችዎ በዚህ መሣሪያ ላይ ብቻ ይቆያሉ።")
        })

        preview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFF101722.toInt())
            contentDescription = if (english) "Local photo preview" else "የፎቶ ቅድመ እይታ"
        }
        guideOverlay = GuideOverlayView(this)
        maskOverlay = ManualMaskView(this)
        val previewFrame = AkiStudioUi.previewContainer(this).apply {
            addView(preview, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(guideOverlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(maskOverlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        page.addView(previewFrame, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(300)
        ).apply { topMargin = dp(5); bottomMargin = dp(10) })

        status = AkiStudioUi.statusText(this)
        status.text = if (english) "READY  ·  Local processing enabled" else "ዝግጁ  ·  በመሣሪያው ላይ የሚሠራ"
        page.addView(status, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(14)
        })

        fun action(symbol: String, en: String, am: String, subEn: String, subAm: String, click: () -> Unit) =
            AkiStudioUi.Action(symbol, if (english) en else am, if (english) subEn else subAm, click)

        fun panel(titleEn: String, titleAm: String, subEn: String, subAm: String, actions: List<AkiStudioUi.Action>) {
            page.addView(AkiStudioUi.panel(
                this,
                if (english) titleEn else titleAm,
                if (english) subEn else subAm,
                actions,
                english
            ), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            })
        }

        panel("Workspace", "የሥራ ቦታ", "Your projects and creative session", "ፕሮጀክቶችዎና የፈጠራ ሥራዎ",
            listOf(
                action("＋", "Open photo", "ፎቶ ክፈት", "Import locally", "በመሣሪያው ላይ ክፈት") { choosePhoto() },
                action("▣", "Save project", "ፕሮጀክት አስቀምጥ", "Keep edits editable", "ለውጦችን ጠብቅ") { saveLocalProject() },
                action("◫", "Open project", "ፕሮጀክት ክፈት", "Resume your work", "ሥራዎን ቀጥል") { openLocalProject() },
                action("⇧", "Save export", "ውጤት አስቀምጥ", "JPEG or PNG copy", "JPEG ወይም PNG") { saveCopy() },
                action("↶", "Undo", "መልስ", "Up to 100 steps", "እስከ 100 ደረጃ") { undoEdit() },
                action("↷", "Redo", "ድገም", "Restore next edit", "ቀጣዩን ለውጥ መልስ") { redoEdit() }
            )
        )

        panel("Image Lab", "የምስል ላብራቶሪ", "Precision editing and tonal control", "ትክክለኛ አርትዖትና የቀለም ቁጥጥር",
            listOf(
                action("⟳", "Rotate", "አዙር", "90° rotation", "90° ማዞር") { rotatePhoto() },
                action("⌗", "Crop", "ቁረጥ", "Aspect presets", "የተወሰነ ምጥጥን") { chooseCrop() },
                action("▱", "Fine crop", "ትክክለኛ ቁረጥ", "Drag and refine", "ጎትተው ያስተካክሉ") { openInteractiveCrop() },
                action("↔", "Resize", "መጠን ቀይር", "Pixel dimensions", "የፒክሰል መጠን") { chooseResize() },
                action("◐", "Adjustments", "ማስተካከያ", "Light and colour", "ብርሃንና ቀለም") { configureImageAdjustments() },
                action("◑", "Monochrome", "ጥቁር ነጭ", "Black and white", "ጥቁርና ነጭ") { toggleMonochrome() },
                action("◉", "Before / After", "ከዚያ በፊት / በኋላ", "Compare changes", "ለውጦችን አወዳድር") { toggleBeforeAfter() },
                action("⟲", "Reset edits", "ለውጦችን መልስ", "Restore original recipe", "ወደ መጀመሪያው ተመለስ") { resetEdits() }
            )
        )

        panel("Portrait & ID", "ፖርትሬትና መታወቂያ", "Guided portrait preparation", "የተመራ የፖርትሬት ዝግጅት",
            listOf(
                action("▣", "ID / Passport", "መታወቂያ / ፓስፖርት", "Dimension presets", "የመጠን አማራጮች") { chooseIdPreset() },
                action("⌖", "Photo guides", "የፎቶ መመሪያ", "Head and eye-line", "የጭንቅላትና የዓይን መስመር") { configurePhotoGuides() },
                action("✓", "Photo standard", "የፎቶ መስፈርት", "Authority-aware rules", "የሥልጣን ምንጭ የሚጠይቅ") { configurePhotoSpecification() },
                action("⌁", "Photo quality", "የፎቶ ጥራት", "Local quality signals", "በመሣሪያው ላይ ጥራት ግምገማ") { assessPhotoQuality() }
            )
        )

        panel("Background Lab", "የጀርባ ላብራቶሪ", "Local masking with manual control", "በመሣሪያው ላይ ማስክና በእጅ ቁጥጥር",
            listOf(
                action("✧", "Quick estimate", "ፈጣን ግምት", "CPU border estimate", "በCPU የጀርባ ግምት") { runLocalForegroundEstimate() },
                action("✎", "Manual mask", "በእጅ ማስክ", "Paint and erase", "ቀለም ቀባና አጥፋ") { configureManualMask() },
                action("◌", "Mask controls", "የማስክ ቁጥጥር", "Brush and edge softness", "ብሩሽና የጠርዝ ልስላሴ") { configureMaskControls() }
            )
        )

        panel("Print Studio", "የህትመት ስቱዲዮ", "Arrange repeat-ready photo sheets", "ለህትመት የተዘጋጁ ፎቶዎችን አደራጅ",
            listOf(
                action("▤", "A4 print sheet", "የA4 ህትመት ወረቀት", "PDF with cut guides", "PDF ከመቁረጫ መመሪያ ጋር") { configurePrintSheet() }
            )
        )

        panel("Design Studio", "የዲዛይን ስቱዲዮ", "Editable layers, type and vector elements", "ሊስተካከሉ የሚችሉ ንብርብሮች፣ ጽሑፍና ቬክተር",
            listOf(
                action("T", "Add typography", "ጽሑፍ ጨምር", "Headlines and captions", "ርዕስና መግለጫ") { addTextLayer() },
                action("◇", "Add vector shape", "ቬክተር ቅርጽ ጨምር", "Rectangle, ellipse, line", "አራት ማዕዘን፣ ክብ፣ መስመር") { addShapeLayer() },
                action("▤", "Manage layers", "ንብርብሮችን አስተዳድር", "Order, visibility, lock", "ቅደም ተከተል፣ ታይነት፣ ቁልፍ") { manageDesignLayers() }
            )
        )

        page.addView(AkiStudioUi.spacer(this, 4))
        page.addView(AkiStudioUi.badge(this,
            if (english) "LOCAL-FIRST  ·  ORIGINALS PRESERVED  ·  NO ACCOUNT" else "በመሣሪያው ላይ ብቻ  · ዋናው ፎቶ ይጠበቃል · መለያ አያስፈልግም",
            AkiStudioUi.MINT
        ))
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(AkiStudioUi.INK)
            addView(page)
        })
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
            editHistory.reset(historySettings())
            showStatus(if (english) "Photo opened locally. Original file is unchanged." else "ፎቶው በመሣሪያው ላይ ተከፍቷል። ዋናው ፋይል አልተቀየረም።")
        } catch (error: Exception) {
            sourceUri = null
            originalPreviewBitmap?.recycle(); originalPreviewBitmap = null
            showStatus(if (english) "Could not open this image: ${error.message ?: "unsupported file"}" else "ፎቶውን መክፈት አልተቻለም።")
        }
    }

    private fun decodeBounded(uri: Uri, maxEdge: Int): Bitmap {
        val source = if (uri.scheme == "file") ImageDecoder.createSource(File(uri.path ?: throw IOException("Invalid local project image"))) else ImageDecoder.createSource(contentResolver, uri)
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
            cropRectNormalized?.let { rect ->
                val cropped = ImageEditorEngine.cropNormalized(image, rect[0], rect[1], rect[2], rect[3])
                image.recycle()
                image = cropped
            } ?: cropRatio?.let { ratio ->
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
            if (brightness != 0 || contrast != 0 || saturation != 100 || warmth != 0) {
                val adjusted = ImageAdjustmentEngine.apply(image, brightness, contrast, saturation, warmth)
                image.recycle()
                image = adjusted
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
            val composited = DesignLayerRenderer.composite(image, designLayers)
            image.recycle()
            image = composited
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
                cropRectNormalized = null
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

    private fun openInteractiveCrop() {
        val uri = sourceUri ?: return noPhoto()
        var source: Bitmap? = null
        try {
            source = decodeBounded(uri, PREVIEW_MAX_EDGE)
            repeat((rotation / 90f).toInt()) {
                val rotated = ImageEditorEngine.rotate(source!!, 90f)
                source?.recycle()
                source = rotated
            }
            val bitmap = source ?: throw IOException("Could not prepare crop image")
            val ratio = cropRatio ?: (bitmap.width.toFloat() / bitmap.height.toFloat())
            val cropView = InteractiveCropView(this, bitmap, ratio, cropRectNormalized)
            val dialog = AlertDialog.Builder(this)
                .setTitle(if (english) "Fine crop — drag the frame or corners" else "ትክክለኛ ቁረጥ — ፍሬሙን ወይም ማዕዘኖቹን ይጎትቱ")
                .setView(cropView)
                .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
                .setPositiveButton(if (english) "Apply crop" else "ቁረጡን ተግብር", null)
                .create()
            dialog.setOnDismissListener { if (!bitmap.isRecycled) bitmap.recycle() }
            dialog.setOnShowListener {
                dialog.window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.94f).toInt(),
                    (resources.displayMetrics.heightPixels * 0.78f).toInt()
                )
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val rect = cropView.normalizedRect()
                    if (rect.size != 4 || rect[2] <= rect[0] || rect[3] <= rect[1]) {
                        showStatus(if (english) "Crop area is invalid." else "የቁረጥ ቦታው ትክክል አይደለም።")
                        return@setOnClickListener
                    }
                    cropRectNormalized = rect
                    cropRatio = ratio
                    cropLabel = if (english) "Fine crop" else "ትክክለኛ ቁረጥ"
                    outputWidthPx = null; outputHeightPx = null
                    resizeMaxWidthPx = null; resizeMaxHeightPx = null
                    dialog.dismiss()
                    refreshPreview()
                }
            }
            dialog.show()
        } catch (error: Exception) {
            source?.recycle()
            showStatus(if (english) "Could not open crop editor: ${error.message ?: "image error"}" else "የቁረጥ አርታዒውን መክፈት አልተቻለም።")
        }
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
                cropRectNormalized = null
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
        cancelSegmentationRequest()
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
            if (!suppressHistoryRecording) editHistory.record(historySettings())
            val description = cropLabel?.let { " · $it" } ?: ""
            showStatus(if (english) "Local edit preview$description. Original is unchanged." else "በመሣሪያው ላይ የተደረገ ማስተካከያ$description። ዋናው ፎቶ አልተቀየረም።")
        } catch (error: Exception) {
            showStatus(if (english) "Could not update preview: ${error.message ?: "image error"}" else "ቅድመ እይታውን ማዘመን አልተቻለም።")
        }
    }

    private fun clearEdits() {
        cancelSegmentationRequest()
        rotation = 0f
        monochrome = false
        brightness = 0
        contrast = 0
        saturation = 100
        warmth = 0
        cropRatio = null
        cropRectNormalized = null
        outputWidthPx = null
        outputHeightPx = null
        resizeMaxWidthPx = null
        resizeMaxHeightPx = null
        cropLabel = null
        cropHorizontal = 0.5f
        cropVertical = 0.5f
        backgroundReplaceEnabled = false
        designLayers.clear()
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

    private fun configureImageAdjustments() {
        if (sourceUri == null) return noPhoto()
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        fun field(label: String, value: Int): EditText {
            val input = EditText(this).apply {
                hint = label
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                setText(value.toString())
            }
            fields.addView(input)
            return input
        }
        val brightnessInput = field(if (english) "Brightness (−100 to 100)" else "ብርሃን (−100 እስከ 100)", brightness)
        val contrastInput = field(if (english) "Contrast (−100 to 100)" else "ንፅፅር (−100 እስከ 100)", contrast)
        val saturationInput = field(if (english) "Saturation (0 to 200; 100 = unchanged)" else "ቀለም ሙላት (0 እስከ 200፤ 100 = መደበኛ)", saturation)
        val warmthInput = field(if (english) "Warmth (−100 cool to 100 warm)" else "ሙቀት (−100 ቀዝቃዛ እስከ 100 ሞቃት)", warmth)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (english) "Non-destructive image adjustments" else "የምስል ማስተካከያዎች")
            .setMessage(if (english) "Values are stored as an edit recipe and re-rendered from the original. They do not repeatedly modify the source." else "እሴቶቹ እንደ ማስተካከያ መመሪያ ይቀመጣሉ፤ ከዋናው ፎቶ እንደገና ይሰራሉ።")
            .setView(fields)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Apply" else "ተግብር", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val b = brightnessInput.text.toString().toIntOrNull()
                val c = contrastInput.text.toString().toIntOrNull()
                val sat = saturationInput.text.toString().toIntOrNull()
                val w = warmthInput.text.toString().toIntOrNull()
                if (b == null || b !in -100..100 || c == null || c !in -100..100 ||
                    sat == null || sat !in 0..200 || w == null || w !in -100..100) {
                    showStatus(if (english) "Brightness, contrast and warmth must be −100 to 100; saturation must be 0 to 200." else "ብርሃን፣ ንፅፅርና ሙቀት −100 እስከ 100፤ ቀለም ሙላት 0 እስከ 200 መሆን አለበት።")
                    return@setOnClickListener
                }
                brightness = b; contrast = c; saturation = sat; warmth = w
                dialog.dismiss()
                refreshPreview()
            }
        }
        dialog.show()
    }

    private fun resetEdits() {
        if (sourceUri == null) return noPhoto()
        clearEdits()
        refreshPreview()
        showStatus(if (english) "Edits reset. Original is unchanged." else "ለውጦቹ ተመልሰዋል። ዋናው ፎቶ አልተቀየረም።")
    }

    /**
     * History deliberately excludes the background mask bitmap and its brush
     * stroke stack. Those have their own dedicated undo/redo controls.
     */
    private fun historySettings(): Map<String, String> = mapOf(
        "rotation" to rotation.toString(),
        "monochrome" to monochrome.toString(),
        "brightness" to brightness.toString(),
        "contrast" to contrast.toString(),
        "saturation" to saturation.toString(),
        "warmth" to warmth.toString(),
        "cropRatio" to (cropRatio?.toString() ?: ""),
        "cropLeft" to (cropRectNormalized?.getOrNull(0)?.toString() ?: ""),
        "cropTop" to (cropRectNormalized?.getOrNull(1)?.toString() ?: ""),
        "cropRight" to (cropRectNormalized?.getOrNull(2)?.toString() ?: ""),
        "cropBottom" to (cropRectNormalized?.getOrNull(3)?.toString() ?: ""),
        "outputWidthPx" to (outputWidthPx?.toString() ?: ""),
        "outputHeightPx" to (outputHeightPx?.toString() ?: ""),
        "resizeMaxWidthPx" to (resizeMaxWidthPx?.toString() ?: ""),
        "resizeMaxHeightPx" to (resizeMaxHeightPx?.toString() ?: ""),
        "cropLabel" to (cropLabel ?: ""),
        "cropHorizontal" to cropHorizontal.toString(),
        "cropVertical" to cropVertical.toString(),
        "designLayers" to DesignLayerDocument.encode(designLayers)
    )

    private fun applyHistorySettings(values: Map<String, String>) {
        rotation = values["rotation"]?.toFloatOrNull()?.coerceIn(0f, 270f) ?: 0f
        monochrome = values["monochrome"].toBoolean()
        brightness = values["brightness"]?.toIntOrNull()?.coerceIn(-100, 100) ?: 0
        contrast = values["contrast"]?.toIntOrNull()?.coerceIn(-100, 100) ?: 0
        saturation = values["saturation"]?.toIntOrNull()?.coerceIn(0, 200) ?: 100
        warmth = values["warmth"]?.toIntOrNull()?.coerceIn(-100, 100) ?: 0
        cropRatio = values["cropRatio"]?.toFloatOrNull()?.takeIf { it.isFinite() && it in 0.1f..10f }
        val crop = listOf("cropLeft", "cropTop", "cropRight", "cropBottom").map { values[it]?.toFloatOrNull() }
        cropRectNormalized = if (crop.all { it != null } &&
            crop.all { it!!.isFinite() && it in 0f..1f } &&
            crop[2]!! > crop[0]!! && crop[3]!! > crop[1]!!) crop.map { it!! }.toFloatArray() else null
        outputWidthPx = values["outputWidthPx"]?.toIntOrNull()?.takeIf { it in 1..4096 }
        outputHeightPx = values["outputHeightPx"]?.toIntOrNull()?.takeIf { it in 1..4096 }
        resizeMaxWidthPx = values["resizeMaxWidthPx"]?.toIntOrNull()?.takeIf { it in 1..4096 }
        resizeMaxHeightPx = values["resizeMaxHeightPx"]?.toIntOrNull()?.takeIf { it in 1..4096 }
        cropLabel = values["cropLabel"]?.takeIf { it.isNotBlank() }
        cropHorizontal = values["cropHorizontal"]?.toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.5f
        cropVertical = values["cropVertical"]?.toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.5f
        designLayers.clear()
        designLayers.addAll(DesignLayerDocument.decode(values["designLayers"]))
    }

    private fun undoEdit() {
        if (sourceUri == null) return noPhoto()
        val previous = editHistory.undo()
        if (previous == null) {
            showStatus(if (english) "No earlier edit to undo." else "የሚመለስ ቀደም ያለ ለውጥ የለም።")
            return
        }
        suppressHistoryRecording = true
        try { applyHistorySettings(previous); refreshPreview() }
        finally { suppressHistoryRecording = false }
        showStatus(if (english) "Edit undone." else "ለውጡ ተመልሷል።")
    }

    private fun redoEdit() {
        if (sourceUri == null) return noPhoto()
        val next = editHistory.redo()
        if (next == null) {
            showStatus(if (english) "No edit to redo." else "እንደገና የሚደረግ ለውጥ የለም።")
            return
        }
        suppressHistoryRecording = true
        try { applyHistorySettings(next); refreshPreview() }
        finally { suppressHistoryRecording = false }
        showStatus(if (english) "Edit restored." else "ለውጡ እንደገና ተተግብሯል።")
    }



    private fun addTextLayer() {
        if (sourceUri == null) return noPhoto()
        if (designLayers.size >= DesignLayerDocument.MAX_LAYERS) {
            showStatus(if (english) "Layer limit reached (30)." else "የንብርብር ገደብ ደርሷል (30)።")
            return
        }
        val content = EditText(this).apply {
            hint = if (english) "Enter text (up to 500 characters)" else "ጽሑፍ ያስገቡ (እስከ 500 ፊደል)"
            minLines = 2
            maxLines = 5
            setText(if (english) "A New Way to Create" else "አዲስ የፈጠራ መንገድ")
        }
        val bold = CheckBox(this).apply { text = if (english) "Bold" else "ደማቅ"; isChecked = true }
        val italic = CheckBox(this).apply { text = if (english) "Italic" else "ሰያፍ" }
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(content); addView(bold); addView(italic)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (english) "Create text layer" else "የጽሑፍ ንብርብር ፍጠር")
            .setView(fields)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Add layer" else "ንብርብር ጨምር", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val text = content.text.toString().trim()
                if (text.isBlank() || text.length > 500) {
                    showStatus(if (english) "Text must contain 1–500 characters." else "ጽሑፉ 1–500 ፊደል መሆን አለበት።")
                    return@setOnClickListener
                }
                designLayers += DesignLayer(
                    name = "Text ${designLayers.count { it.kind == "TEXT" } + 1}",
                    kind = "TEXT", text = text, bold = bold.isChecked, italic = italic.isChecked
                )
                dialog.dismiss()
                refreshPreview()
                showStatus(if (english) "Editable text layer added." else "ሊስተካከል የሚችል የጽሑፍ ንብርብር ተጨምሯል።")
            }
        }
        dialog.show()
    }

    private fun addShapeLayer() {
        if (sourceUri == null) return noPhoto()
        if (designLayers.size >= DesignLayerDocument.MAX_LAYERS) {
            showStatus(if (english) "Layer limit reached (30)." else "የንብርብር ገደብ ደርሷል (30)።")
            return
        }
        val shapes = arrayOf("Rectangle", "Ellipse", "Line")
        val types = arrayOf("RECTANGLE", "ELLIPSE", "LINE")
        AlertDialog.Builder(this)
            .setTitle(if (english) "Choose vector primitive" else "የቬክተር ቅርጽ ይምረጡ")
            .setItems(shapes) { _, which ->
                designLayers += DesignLayer(
                    name = "${shapes[which]} ${designLayers.count { it.kind == "SHAPE" } + 1}",
                    kind = "SHAPE", shape = types[which],
                    fillColor = if (types[which] == "LINE") android.graphics.Color.TRANSPARENT else 0x5575E0C0,
                    strokeColor = AkiStudioUi.MINT
                )
                refreshPreview()
                showStatus(if (english) "Editable vector layer added." else "ሊስተካከል የሚችል ቬክተር ተጨምሯል።")
            }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
    }

    private fun manageDesignLayers() {
        if (sourceUri == null) return noPhoto()
        if (designLayers.isEmpty()) {
            showStatus(if (english) "No design layers yet. Add text or a vector shape first." else "ገና ንብርብር የለም። ጽሑፍ ወይም ቬክተር ይጨምሩ።")
            return
        }
        val labels = designLayers.asReversed().mapIndexed { index, layer ->
            "${if (layer.visible) "●" else "○"}  ${if (layer.locked) "🔒" else ""}${layer.name}  ·  ${layer.kind}"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(if (english) "Layers — top to bottom" else "ንብርብሮች — ከላይ ወደ ታች")
            .setItems(labels) { _, reverseIndex ->
                editDesignLayer(designLayers.lastIndex - reverseIndex)
            }
            .setNegativeButton(if (english) "Close" else "ዝጋ", null)
            .show()
    }

    private fun editDesignLayer(index: Int) {
        if (index !in designLayers.indices) return
        val layer = designLayers[index]
        val name = EditText(this).apply { hint = if (english) "Layer name" else "የንብርብር ስም"; setSingleLine(true); setText(layer.name) }
        val x = EditText(this).apply { hint = "Center X (0–100%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText((layer.x * 100).toInt().toString()) }
        val y = EditText(this).apply { hint = "Center Y (0–100%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText((layer.y * 100).toInt().toString()) }
        val width = EditText(this).apply { hint = "Width (1–100%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText((layer.width * 100).toInt().toString()) }
        val height = EditText(this).apply { hint = "Height (1–100%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText((layer.height * 100).toInt().toString()) }
        val opacity = EditText(this).apply { hint = "Opacity (0–100%)"; inputType = InputType.TYPE_CLASS_NUMBER; setText((layer.opacity * 100).toInt().toString()) }
        val visible = CheckBox(this).apply { text = if (english) "Visible" else "የሚታይ"; isChecked = layer.visible }
        val locked = CheckBox(this).apply { text = if (english) "Lock layer" else "ንብርብሩን ቆልፍ"; isChecked = layer.locked }
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            listOf(name, x, y, width, height, opacity, visible, locked).forEach { addView(it) }
        }
        val actions = arrayOf(
            if (english) "Save properties" else "ባህሪያት አስቀምጥ",
            if (english) "Edit text" else "ጽሑፍ አስተካክል",
            if (english) "Move up" else "ወደ ላይ አንቀሳቅስ",
            if (english) "Move down" else "ወደ ታች አንቀሳቅስ",
            if (english) "Delete layer" else "ንብርብር ሰርዝ"
        )
        AlertDialog.Builder(this)
            .setTitle(layer.name)
            .setItems(actions) { _, action ->
                when (action) {
                    0 -> {
                        val nx = name.text.toString().trim()
                        val px = x.text.toString().toIntOrNull()
                        val py = y.text.toString().toIntOrNull()
                        val pw = width.text.toString().toIntOrNull()
                        val ph = height.text.toString().toIntOrNull()
                        val po = opacity.text.toString().toIntOrNull()
                        if (nx.isBlank() || nx.length > 48 || px !in 0..100 || py !in 0..100 ||
                            pw !in 1..100 || ph !in 1..100 || po !in 0..100) {
                            showStatus(if (english) "Layer properties are outside their allowed ranges." else "የንብርብር ባህሪያት ከተፈቀደው ክልል ውጭ ናቸው።")
                            return@setItems
                        }
                        designLayers[index] = layer.copy(
                            name = nx, x = px!! / 100f, y = py!! / 100f,
                            width = pw!! / 100f, height = ph!! / 100f,
                            opacity = po!! / 100f, visible = visible.isChecked, locked = locked.isChecked
                        ).validated()
                        refreshPreview()
                    }
                    1 -> if (layer.kind == "TEXT") editLayerText(index) else showStatus(if (english) "This layer is a vector shape." else "ይህ ንብርብር ቬክተር ቅርጽ ነው።")
                    2 -> { if (index < designLayers.lastIndex) { val item = designLayers.removeAt(index); designLayers.add(index + 1, item); refreshPreview() } }
                    3 -> { if (index > 0) { val item = designLayers.removeAt(index); designLayers.add(index - 1, item); refreshPreview() } }
                    4 -> AlertDialog.Builder(this)
                        .setMessage(if (english) "Delete this design layer?" else "ይህ የዲዛይን ንብርብር ይሰረዝ?")
                        .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
                        .setPositiveButton(if (english) "Delete" else "ሰርዝ") { _, _ ->
                            designLayers.removeAt(index)
                            refreshPreview()
                        }.show()
                }
            }
            .setNegativeButton(if (english) "Close" else "ዝጋ", null)
            .setView(fields)
            .show()
    }

    private fun editLayerText(index: Int) {
        val layer = designLayers.getOrNull(index) ?: return
        if (layer.locked) {
            showStatus(if (english) "Unlock this layer before editing it." else "ከማስተካከልዎ በፊት ንብርብሩን ይክፈቱ።")
            return
        }
        val input = EditText(this).apply { minLines = 2; maxLines = 6; setText(layer.text) }
        AlertDialog.Builder(this)
            .setTitle(if (english) "Edit typography" else "ጽሑፍ አስተካክል")
            .setView(input)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Apply" else "ተግብር") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotBlank() && text.length <= 500) {
                    designLayers[index] = layer.copy(text = text).validated()
                    refreshPreview()
                }
            }
            .show()
    }

    private fun saveLocalProject() {
        val uri = sourceUri ?: return noPhoto()
        val nameInput = EditText(this).apply {
            hint = if (english) "Project name" else "የፕሮጀክት ስም"
            setSingleLine(true)
            setText(cropLabel?.take(40) ?: if (english) "My photo project" else "የእኔ የፎቶ ፕሮጀክት")
        }
        AlertDialog.Builder(this)
            .setTitle(if (english) "Save editable project" else "ሊስተካከል የሚችል ፕሮጀክት አስቀምጥ")
            .setMessage(if (english) "A private copy of the original image and your edit settings will be stored on this device. Nothing is uploaded." else "የዋናው ፎቶ ቅጂና የማስተካከያ ቅንብሮች በዚህ መሣሪያ ብቻ ይቀመጣሉ። ምንም ነገር አይላክም።")
            .setView(nameInput)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Save project" else "አስቀምጥ", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = nameInput.text.toString().trim()
                        if (name.isBlank() || name.length > 60) {
                            showStatus(if (english) "Enter a project name up to 60 characters." else "እስከ 60 ፊደል ያለውን የፕሮጀክት ስም ያስገቡ።")
                            return@setOnClickListener
                        }
                        try {
                            val input = if (uri.scheme == "file") FileInputStream(File(uri.path ?: throw IOException("Invalid source path"))) else contentResolver.openInputStream(uri)
                                ?: throw IOException("Could not read original image")
                            val mask = if (backgroundReplaceEnabled) {
                                autoMaskBitmap?.copy(Bitmap.Config.ARGB_8888, false)
                                    ?: previewBitmap?.let { maskOverlay.renderMask(it.width, it.height) }
                            } else null
                            val summary = try {
                                LocalProjectStore.save(filesDir, name, input, projectSettings(), mask)
                            } finally {
                                mask?.recycle()
                            }
                            dialog.dismiss()
                            showStatus(if (english) "Project saved privately on this device: ${summary.name}" else "ፕሮጀክቱ በዚህ መሣሪያ ብቻ ተቀምጧል፦ ${summary.name}")
                        } catch (error: Exception) {
                            showStatus(if (english) "Project save failed: ${error.message ?: "storage error"}" else "ፕሮጀክቱን ማስቀመጥ አልተሳካም።")
                        }
                    }
                }
                dialog.show()
            }
    }

    private fun projectSettings(): Map<String, String> = mapOf(
        "rotation" to rotation.toString(),
        "monochrome" to monochrome.toString(),
        "brightness" to brightness.toString(),
        "contrast" to contrast.toString(),
        "saturation" to saturation.toString(),
        "warmth" to warmth.toString(),
        "cropRatio" to (cropRatio?.toString() ?: ""),
        "cropLeft" to (cropRectNormalized?.get(0)?.toString() ?: ""),
        "cropTop" to (cropRectNormalized?.get(1)?.toString() ?: ""),
        "cropRight" to (cropRectNormalized?.get(2)?.toString() ?: ""),
        "cropBottom" to (cropRectNormalized?.get(3)?.toString() ?: ""),
        "outputWidthPx" to (outputWidthPx?.toString() ?: ""),
        "outputHeightPx" to (outputHeightPx?.toString() ?: ""),
        "resizeMaxWidthPx" to (resizeMaxWidthPx?.toString() ?: ""),
        "resizeMaxHeightPx" to (resizeMaxHeightPx?.toString() ?: ""),
        "cropLabel" to (cropLabel ?: ""),
        "cropHorizontal" to cropHorizontal.toString(),
        "cropVertical" to cropVertical.toString(),
        "backgroundReplaceEnabled" to backgroundReplaceEnabled.toString(),
        "backgroundColor" to backgroundColor.toString(),
        "headHeightPercent" to headHeightPercent.toString(),
        "eyeLinePercent" to eyeLinePercent.toString(),
        "guidesVisible" to guidesVisible.toString(),
        "designLayers" to DesignLayerDocument.encode(designLayers)
    )

    private fun openLocalProject() {
        val projects = LocalProjectStore.list(filesDir)
        if (projects.isEmpty()) {
            showStatus(if (english) "No saved projects on this device yet." else "በዚህ መሣሪያ ላይ የተቀመጠ ፕሮጀክት የለም።")
            return
        }
        val labels = projects.map { summary ->
            val date = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(summary.updatedAt))
            "${summary.name} — $date"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(if (english) "Manage local projects" else "የተቀመጡ ፕሮጀክቶችን ያስተዳድሩ")
            .setItems(labels) { _, which -> showProjectActions(projects[which]) }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
    }

    private fun showProjectActions(summary: LocalProjectStore.ProjectSummary) {
        val actions = arrayOf(
            if (english) "Open" else "ክፈት",
            if (english) "Duplicate" else "ኮፒ አድርግ",
            if (english) "Rename" else "ስም ቀይር",
            if (english) "Delete" else "ሰርዝ"
        )
        AlertDialog.Builder(this)
            .setTitle(summary.name)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> restoreLocalProject(summary)
                    1 -> runCatching { LocalProjectStore.duplicate(filesDir, summary) }
                        .onSuccess { showStatus(if (english) "Project duplicated: ${it.name}" else "ፕሮጀክቱ ተባዝቷል፦ ${it.name}") }
                        .onFailure { showStatus(if (english) "Duplicate failed: ${it.message}" else "ኮፒ ማድረግ አልተሳካም።") }
                    2 -> renameLocalProject(summary)
                    3 -> AlertDialog.Builder(this)
                        .setTitle(if (english) "Delete project?" else "ፕሮጀክቱ ይሰረዝ?")
                        .setMessage(if (english) "This permanently deletes the private project copy and its edit settings. It does not delete the original image outside Aki Studio." else "ይህ በመተግበሪያው ውስጥ ያለውን የፕሮጀክት ቅጂና ቅንብሮቹን በቋሚነት ይሰርዛል። ከAki Studio ውጭ ያለውን ዋና ፎቶ አይሰርዝም።")
                        .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
                        .setPositiveButton(if (english) "Delete" else "ሰርዝ") { _, _ ->
                            val deleted = LocalProjectStore.delete(summary)
                            showStatus(if (deleted) {
                                if (english) "Project deleted from this device." else "ፕሮጀክቱ ከዚህ መሣሪያ ተሰርዟል።"
                            } else {
                                if (english) "Project could not be deleted." else "ፕሮጀክቱን መሰረዝ አልተቻለም።"
                            })
                        }.show()
                }
            }
            .setNegativeButton(if (english) "Back" else "ተመለስ", null)
            .show()
    }

    private fun renameLocalProject(summary: LocalProjectStore.ProjectSummary) {
        val input = EditText(this).apply { setSingleLine(true); setText(summary.name) }
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (english) "Rename project" else "የፕሮጀክት ስም ቀይር")
            .setView(input)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Save" else "አስቀምጥ", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = input.text.toString().trim()
                if (name.isBlank() || name.length > 60) {
                    showStatus(if (english) "Project name must contain 1–60 characters." else "የፕሮጀክት ስም 1–60 ፊደል መሆን አለበት።")
                    return@setOnClickListener
                }
                runCatching { LocalProjectStore.rename(summary, name) }
                    .onSuccess { showStatus(if (english) "Project renamed: ${it.name}" else "የፕሮጀክቱ ስም ተቀይሯል፦ ${it.name}") }
                    .onFailure { showStatus(if (english) "Rename failed: ${it.message}" else "ስም መቀየር አልተሳካም።") }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun restoreLocalProject(summary: LocalProjectStore.ProjectSummary) {
        try {
            val data = LocalProjectStore.load(summary)
            val projectUri = Uri.fromFile(data.sourceFile)
            val validationPreview = decodeBounded(projectUri, ORIGINAL_PREVIEW_MAX_EDGE)
            validationPreview.recycle()
            cancelSegmentationRequest()
            sourceUri = projectUri
            clearEdits()
            val values = data.values
            rotation = values["rotation"]?.toFloatOrNull()?.takeIf { it.isFinite() }?.rem(360f) ?: 0f
            monochrome = values["monochrome"].toBoolean()
            brightness = values["brightness"]?.toIntOrNull()?.coerceIn(-100, 100) ?: 0
            contrast = values["contrast"]?.toIntOrNull()?.coerceIn(-100, 100) ?: 0
            saturation = values["saturation"]?.toIntOrNull()?.coerceIn(0, 200) ?: 100
            warmth = values["warmth"]?.toIntOrNull()?.coerceIn(-100, 100) ?: 0
            cropRatio = values["cropRatio"]?.toFloatOrNull()?.takeIf { it.isFinite() && it > 0f }
            cropRectNormalized = listOf("cropLeft", "cropTop", "cropRight", "cropBottom").map { values[it]?.toFloatOrNull() }.takeIf { it.all { value -> value != null } }?.map { it!! }?.toFloatArray()
            outputWidthPx = values["outputWidthPx"]?.toIntOrNull()
            outputHeightPx = values["outputHeightPx"]?.toIntOrNull()
            resizeMaxWidthPx = values["resizeMaxWidthPx"]?.toIntOrNull()
            resizeMaxHeightPx = values["resizeMaxHeightPx"]?.toIntOrNull()
            cropLabel = values["cropLabel"]?.takeIf { it.isNotBlank() }
            cropHorizontal = values["cropHorizontal"]?.toFloatOrNull()?.coerceIn(0f, 1f) ?: 0.5f
            cropVertical = values["cropVertical"]?.toFloatOrNull()?.coerceIn(0f, 1f) ?: 0.5f
            backgroundReplaceEnabled = values["backgroundReplaceEnabled"].toBoolean()
            backgroundColor = values["backgroundColor"]?.toIntOrNull() ?: android.graphics.Color.WHITE
            headHeightPercent = values["headHeightPercent"]?.toIntOrNull()?.coerceIn(40, 90) ?: 70
            eyeLinePercent = values["eyeLinePercent"]?.toIntOrNull()?.coerceIn(20, 70) ?: 42
            guidesVisible = values["guidesVisible"].toBoolean()
            designLayers.clear()
            designLayers.addAll(DesignLayerDocument.decode(values["designLayers"]))
            autoMaskBitmap = data.mask
            previewBitmap?.recycle()
            originalPreviewBitmap?.recycle()
            originalPreviewBitmap = decodeBounded(sourceUri!!, ORIGINAL_PREVIEW_MAX_EDGE)
            previewBitmap = buildEditedBitmap(PREVIEW_MAX_EDGE)
            if (backgroundReplaceEnabled && autoMaskBitmap != null) maskOverlay.setBaseMask(autoMaskBitmap)
            renderPreview()
            editHistory.reset(historySettings())
            showStatus(if (english) "Project reopened locally. Original image and editable settings restored." else "ፕሮጀክቱ በመሣሪያው ላይ እንደገና ተከፍቷል። ዋናው ፎቶና ቅንብሮቹ ተመልሰዋል።")
        } catch (error: Exception) {
            showStatus(if (english) "Could not open project: ${error.message ?: "project error"}" else "ፕሮጀክቱን መክፈት አልተቻለም።")
        }
    }

    private fun saveCopy() {
        if (sourceUri == null) return noPhoto()
        val formats = arrayOf(
            if (english) "JPEG — smaller photographic file" else "JPEG — የታመቀ የፎቶ ፋይል",
            if (english) "PNG — lossless, supports transparency" else "PNG — ጥራት የማይቀንስ፣ ግልጽነትን ይደግፋል"
        )
        AlertDialog.Builder(this)
            .setTitle(if (english) "Choose export format" else "የማስቀመጫ ቅርጸት ይምረጡ")
            .setItems(formats) { _, which ->
                confirmSaveCopy(if (which == 1) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG)
            }
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .show()
    }

    private fun requestExportFileName(type: String, extension: String, onReady: (String) -> Unit) {
        val preferences = getSharedPreferences("export_naming", MODE_PRIVATE)
        val patternInput = EditText(this).apply {
            hint = if (english) "File name or pattern" else "የፋይል ስም ወይም አቀማመጥ"
            setSingleLine(true)
            setText(preferences.getString("pattern", ExportFileNaming.DEFAULT_PATTERN))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        val saveDefault = CheckBox(this).apply {
            text = if (english) "Save this as my default filename pattern" else "ይህን እንደ ነባሪ የፋይል ስም አቀማመጤ አስቀምጥ"
            isChecked = false
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(patternInput)
            addView(TextView(this@MainActivity).apply {
                text = if (english) "Tokens: {type} and {date}. Example: Studio_{type}_{date}. You may also enter a fixed name." else "ምልክቶች፦ {type} እና {date}። ምሳሌ፦ Studio_{type}_{date}። ቋሚ ስምም ማስገባት ይችላሉ።"
                textSize = 12f
                setPadding(0, dp(6), 0, dp(6))
            })
            addView(saveDefault)
        }
        AlertDialog.Builder(this)
            .setTitle(if (english) "Name your file" else "የፋይሉን ስም ይስጡ")
            .setMessage(if (english) "Choose a one-time name or save your own reusable naming pattern. The file is saved locally." else "ለዚህ ጊዜ ብቻ ስም ይምረጡ ወይም የራስዎን ተደጋጋሚ የስም አቀማመጥ ያስቀምጡ። ፋይሉ በመሣሪያው ላይ ይቀመጣል።")
            .setView(content)
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Continue" else "ቀጥል", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val rawPattern = patternInput.text.toString().trim()
                        try {
                            require(rawPattern.isNotBlank()) { "Enter a file name or naming pattern" }
                            val expanded = ExportFileNaming.expand(rawPattern, type)
                            val safeName = ExportFileNaming.safeBaseName(expanded, extension)
                            if (saveDefault.isChecked) {
                                preferences.edit().putString("pattern", rawPattern).apply()
                            }
                            dialog.dismiss()
                            onReady(safeName)
                        } catch (error: Exception) {
                            showStatus(if (english) "Invalid file name: ${error.message}" else "የፋይል ስሙ ትክክል አይደለም።")
                        }
                    }
                }
                dialog.show()
            }
    }

    private fun confirmSaveCopy(format: Bitmap.CompressFormat) {
        val dimensions = (previewBitmap?.width ?: 0).toString() + " × " + (previewBitmap?.height ?: 0) + " px"
        val isPng = format == Bitmap.CompressFormat.PNG
        val label = if (isPng) "PNG" else "JPEG"
        AlertDialog.Builder(this)
            .setTitle(if (english) "Confirm export" else "የፎቶ ማስቀመጥን ያረጋግጡ")
            .setMessage(if (english) "Save a new $label copy ($dimensions) to Pictures/Aki Studio? The original file will not be changed." else "አዲስ $label ቅጂ ($dimensions) በPictures/Aki Studio ይቀመጥ? ዋናው ፋይል አይቀየርም።")
            .setNegativeButton(if (english) "Cancel" else "ሰርዝ", null)
            .setPositiveButton(if (english) "Save copy" else "ቅጂ አስቀምጥ") { _, _ ->
                requestExportFileName("photo", if (isPng) "png" else "jpg") { fileName -> performSaveCopy(format, fileName) }
            }
            .show()
    }

    private fun performSaveCopy(format: Bitmap.CompressFormat, fileName: String) {
        if (sourceUri == null) return noPhoto()
        val isPng = format == Bitmap.CompressFormat.PNG
        val extension = if (isPng) "png" else "jpg"
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val label = if (isPng) "PNG" else "JPEG"
        val quality = if (isPng) 100 else 94
        try {
            val output = buildEditedBitmap(EXPORT_MAX_EDGE)
            try {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Aki Studio")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val destination = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Could not create output file")
                try {
                    contentResolver.openOutputStream(destination)?.use { stream ->
                        if (!output.compress(format, quality, stream)) {
                            throw IOException("$extension encoding failed")
                        }
                    } ?: throw IOException("Could not write output")
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(destination, values, null, null)
                    showStatus(if (english) "Saved a new $label copy in Pictures/Aki Studio." else "አዲስ $label ቅጂ በPictures/Aki Studio ተቀምጧል።")
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

    private fun cancelSegmentationRequest() {
        segmentationRequestId += 1
        segmentationFuture?.cancel(true)
        segmentationFuture = null
    }

    private fun runLocalForegroundEstimate() {
        val currentPreview = previewBitmap
        if (sourceUri == null || currentPreview == null || currentPreview.isRecycled) return noPhoto()

        // Invalidate any older request and give the worker its own immutable pixel snapshot.
        segmentationRequestId += 1
        val requestId = segmentationRequestId
        val requestedBackgroundColor = backgroundColor
        segmentationFuture?.cancel(true)
        val sourceSnapshot = try {
            currentPreview.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw IOException("Could not copy preview for local analysis")
        } catch (error: Exception) {
            showStatus(if (english) "Could not prepare local analysis: ${error.message ?: "memory limit"}" else "ለአካባቢያዊ ግምት ፎቶውን ማዘጋጀት አልተቻለም።")
            return
        }

        showStatus(if (english) "Estimating the background on this device… You can keep using the app." else "በዚህ መሣሪያ ላይ የጀርባው ክፍል እየተገመተ ነው… መተግበሪያውን መጠቀም ይችላሉ።")
        segmentationFuture = segmentationExecutor.submit {
            var result: SegmentationResult? = null
            var replacement: Bitmap? = null
            var failure: Exception? = null
            val bundledModel = LiteRtSelfieSegmenter.createOrNull(applicationContext)
            try {
                result = LocalForegroundSegmentationEngine(model = bundledModel).segment(sourceSnapshot)
                replacement = BackgroundReplacementEngine.replaceSolid(sourceSnapshot, result.foregroundMask, requestedBackgroundColor)
            } catch (error: Exception) {
                failure = error
            } finally {
                bundledModel?.close()
                sourceSnapshot.recycle()
            }

            runOnUiThread {
                if (isFinishing || isDestroyed || requestId != segmentationRequestId) {
                    result?.foregroundMask?.recycle()
                    replacement?.recycle()
                    return@runOnUiThread
                }
                val completed = result
                val preview = replacement
                if (failure != null || completed == null || preview == null) {
                    completed?.foregroundMask?.recycle()
                    preview?.recycle()
                    showStatus(if (english) "Local foreground estimate failed: ${failure?.message ?: "image error"}. Your original photo is unchanged." else "የአካባቢያዊ ግምት አልተሳካም። ዋናው ፎቶ አልተቀየረም።")
                    return@runOnUiThread
                }
                autoMaskBitmap?.recycle()
                autoPreviewBitmap?.recycle()
                autoMaskBitmap = completed.foregroundMask
                autoPreviewBitmap = preview
                backgroundReplaceEnabled = true
                maskOverlay.configure(previewBitmap, false)
                renderPreview()
                val caveat = if (completed.method.startsWith("Bundled LiteRT")) {
                    if (english) {
                        "Local model estimate: " + completed.method + ". Mask scores are not calibrated confidence values. Inspect and correct the mask manually; fine hair and edges may be inaccurate."
                    } else {
                        "የመሣሪያ ሞዴል ግምት፦ " + completed.method + "። የማስክ ውጤቶች የተረጋገጡ የእምነት መጠኖች አይደሉም። ማስኩን ይመርምሩና በእጅ ያስተካክሉ።"
                    }
                } else if (english) {
                    "Local estimate: " + completed.method + ". Low-confidence border-colour heuristic only; inspect and correct the mask manually. Similar-coloured hair/clothing may be removed."
                } else {
                    "በመሣሪያው ላይ የተገመተ፦ " + completed.method + "። ዝቅተኛ እምነት ያለው የጠርዝ ቀለም ግምት ብቻ ነው፤ ማስኩን ይመርምሩና በእጅ ያስተካክሉ።"
                }
                showStatus(caveat)
            }
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
                        if (autoMaskBitmap != null) {
                            maskOverlay.setBaseMask(autoMaskBitmap)
                            autoMaskBitmap?.recycle(); autoMaskBitmap = null
                            autoPreviewBitmap?.recycle(); autoPreviewBitmap = null
                        }
                        backgroundReplaceEnabled = true
                        preview.setImageBitmap(previewBitmap)
                        maskOverlay.configure(previewBitmap, true, true)
                        showStatus(if (english) "Erase unwanted mask regions." else "የማይፈለጉ የማስክ ክፍሎችን ያጥፉ።")
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
                requestExportFileName("print_sheet", "pdf") { fileName -> exportPrintSheet(columns, rows, fileName) }
            }
        }
        dialog.show()
    }

    private fun exportPrintSheet(columns: Int, rows: Int, fileName: String) {
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
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
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
        preview.setImageBitmap(if (showingOriginal) originalPreviewBitmap else (autoPreviewBitmap ?: previewBitmap))
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
        segmentationRequestId += 1
        segmentationFuture?.cancel(true)
        segmentationExecutor.shutdownNow()
        previewBitmap?.recycle()
        previewBitmap = null
        originalPreviewBitmap?.recycle()
        originalPreviewBitmap = null
        autoMaskBitmap?.recycle(); autoMaskBitmap = null
        autoPreviewBitmap?.recycle(); autoPreviewBitmap = null
        if (::maskOverlay.isInitialized) maskOverlay.clearMask()
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
