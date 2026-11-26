package com.akidigitalstudio.aki

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Filename-pattern expansion and safe MediaStore display-name validation. */
object ExportFileNaming {
    const val DEFAULT_PATTERN = "aki_{type}_{date}"

    fun expand(pattern: String, type: String, timestamp: Long = System.currentTimeMillis()): String {
        val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date(timestamp))
        return pattern.trim()
            .replace("{type}", type)
            .replace("{date}", date)
    }

    fun safeBaseName(value: String, extension: String): String {
        val ext = ".$extension"
        val withoutExtension = value.trim().let {
            if (it.endsWith(ext, ignoreCase = true)) it.dropLast(ext.length) else it
        }
        val safe = withoutExtension
            .replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .trim('.', '_', ' ')
            .take(80)
        require(safe.isNotBlank()) { "A valid file name is required" }
        require(safe != "." && safe != "..") { "Invalid file name" }
        return "$safe$ext"
    }
}
