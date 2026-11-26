package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Properties
import java.util.UUID

/**
 * Private, offline project storage. A project owns a byte-for-byte copy of its
 * original image plus a small edit recipe and optional flattened mask.
 */
object LocalProjectStore {
    data class ProjectSummary(
        val id: String,
        val name: String,
        val updatedAt: Long,
        val directory: File
    )

    data class ProjectData(
        val summary: ProjectSummary,
        val sourceFile: File,
        val values: Map<String, String>,
        val mask: Bitmap?
    )

    private const val MANIFEST = "project.properties"
    private const val SOURCE = "original.img"
    private const val MASK = "foreground-mask.png"

    fun save(
        root: File,
        name: String,
        source: InputStream,
        values: Map<String, String>,
        mask: Bitmap?
    ): ProjectSummary {
        val safeName = name.trim().take(60)
        require(safeName.isNotBlank()) { "Project name is required" }
        val projects = File(root, "projects").apply { mkdirs() }
        val id = UUID.randomUUID().toString()
        val directory = File(projects, id)
        check(directory.mkdir()) { "Could not create project directory" }
        try {
            val sourceFile = File(directory, SOURCE)
            source.use { input ->
                FileOutputStream(sourceFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_SOURCE_BYTES) { "Original image exceeds the 100 MB project limit" }
                        output.write(buffer, 0, count)
                    }
                }
            }
            check(sourceFile.length() > 0L) { "Source image is empty" }
            require(values.size <= 32 && values.all { (key, value) ->
                key.matches(Regex("[A-Za-z][A-Za-z0-9]*")) && value.length <= 2048
            }) { "Project edit settings are invalid" }
            val maskFile = File(directory, MASK)
            val hasMask = mask?.let { bitmap ->
                FileOutputStream(maskFile).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Could not encode project mask" }
                }
                true
            } ?: false
            val now = System.currentTimeMillis()
            val properties = Properties().apply {
                setProperty("schemaVersion", "1")
                setProperty("id", id)
                setProperty("name", safeName)
                setProperty("updatedAt", now.toString())
                setProperty("hasMask", hasMask.toString())
                values.forEach { (key, value) ->
                    require(key.matches(Regex("[A-Za-z][A-Za-z0-9]*"))) { "Invalid project field" }
                    setProperty("edit.$key", value)
                }
            }
            FileOutputStream(File(directory, MANIFEST)).use { properties.store(it, "Aki Studio local project v1") }
            return ProjectSummary(id, safeName, now, directory)
        } catch (error: Exception) {
            directory.deleteRecursively()
            throw error
        }
    }

    fun list(root: File): List<ProjectSummary> {
        val projects = File(root, "projects")
        return projects.listFiles()?.mapNotNull { directory ->
            val manifest = File(directory, MANIFEST)
            if (!manifest.isFile) return@mapNotNull null
            runCatching {
                val p = Properties().apply { FileInputStream(manifest).use(::load) }
                ProjectSummary(
                    id = p.getProperty("id") ?: return@runCatching null,
                    name = p.getProperty("name") ?: "Untitled project",
                    updatedAt = p.getProperty("updatedAt")?.toLongOrNull() ?: 0L,
                    directory = directory
                )
            }.getOrNull()
        }?.sortedByDescending { it.updatedAt } ?: emptyList()
    }

    fun load(summary: ProjectSummary): ProjectData {
        val manifest = File(summary.directory, MANIFEST)
        require(manifest.isFile) { "Project manifest is missing" }
        val p = Properties().apply { FileInputStream(manifest).use(::load) }
        require(p.getProperty("schemaVersion") == "1") { "Unsupported project version" }
        val expectedParent = File(summary.directory.parentFile, summary.id).canonicalFile
        require(summary.directory.canonicalFile == expectedParent && summary.directory.parentFile?.name == "projects") {
            "Project directory is outside the private project store"
        }
        require(p.getProperty("id") == summary.id) { "Project identifier does not match its manifest" }
        val source = File(summary.directory, SOURCE)
        require(source.isFile && source.length() in 1..MAX_SOURCE_BYTES) { "Project original image is missing or exceeds the size limit" }
        val mask = if (p.getProperty("hasMask") == "true") {
            val file = File(summary.directory, MASK)
            require(file.isFile) { "Project mask is missing" }
            BitmapFactory.decodeFile(file.absolutePath) ?: throw IllegalStateException("Project mask is invalid")
        } else null
        val values = p.stringPropertyNames()
            .filter { it.startsWith("edit.") }
            .associate { it.removePrefix("edit.") to p.getProperty(it) }
        return ProjectData(summary, source, values, mask)
    }

    fun duplicate(root: File, summary: ProjectSummary): ProjectSummary {
        val data = load(summary)
        return try {
            FileInputStream(data.sourceFile).use { input ->
                save(root, "${summary.name} copy", input, data.values, data.mask)
            }
        } finally {
            data.mask?.recycle()
        }
    }

    fun rename(summary: ProjectSummary, newName: String): ProjectSummary {
        val safeName = newName.trim().take(60)
        require(safeName.isNotBlank()) { "Project name is required" }
        val manifest = File(summary.directory, MANIFEST)
        val properties = Properties().apply { FileInputStream(manifest).use(::load) }
        val now = System.currentTimeMillis()
        properties.setProperty("name", safeName)
        properties.setProperty("updatedAt", now.toString())
        FileOutputStream(manifest).use { properties.store(it, "Aki Studio local project v1") }
        return summary.copy(name = safeName, updatedAt = now)
    }

    private const val MAX_SOURCE_BYTES = 100L * 1024L * 1024L

    fun delete(summary: ProjectSummary): Boolean {
        val root = summary.directory.parentFile?.canonicalFile ?: return false
        val expected = File(root, summary.id).canonicalFile
        if (expected != summary.directory.canonicalFile || root.name != "projects") return false
        return summary.directory.deleteRecursively()
    }
}
