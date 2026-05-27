package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class NovelPage(
    val id: String = UUID.randomUUID().toString(),
    val translatedText: String,
    val originalText: String? = null,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object NovelRepository {
    private val gson = Gson()

    private fun getNovelFile(context: Context, novelName: String): File {
        return File(context.filesDir, "novel_${novelName}.json")
    }

    private fun getImagesDir(context: Context, novelName: String): File {
        val dir = File(context.filesDir, "images_${novelName}")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveTranslation(
        context: Context,
        novelName: String,
        translatedText: String,
        originalText: String? = null,
        bitmap: Bitmap? = null
    ) {
        val pages = getPages(context, novelName).toMutableList()
        
        var imagePath: String? = null
        if (bitmap != null) {
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(getImagesDir(context, novelName), fileName)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                imagePath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val newPage = NovelPage(
            translatedText = translatedText,
            originalText = originalText,
            imagePath = imagePath
        )
        pages.add(newPage)

        try {
            getNovelFile(context, novelName).writeText(gson.toJson(pages))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePages(context: Context, novelName: String, pages: List<NovelPage>) {
        try {
            getNovelFile(context, novelName).writeText(gson.toJson(pages))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPages(context: Context, novelName: String): List<NovelPage> {
        val file = getNovelFile(context, novelName)
        if (!file.exists()) return emptyList()

        return try {
            val json = file.readText()
            val type = object : TypeToken<List<NovelPage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteNovelData(context: Context, novelName: String) {
        getNovelFile(context, novelName).delete()
        getImagesDir(context, novelName).deleteRecursively()
    }

    fun getCoverFile(context: Context, novelName: String): File {
        return File(getImagesDir(context, novelName), "cover.jpg")
    }

    fun saveCoverImage(context: Context, novelName: String, uri: Uri) {
        val coverFile = getCoverFile(context, novelName)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(coverFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteCoverImage(context: Context, novelName: String) {
        val coverFile = getCoverFile(context, novelName)
        if (coverFile.exists()) {
            coverFile.delete()
        }
    }

    fun renameNovelData(context: Context, oldName: String, newName: String) {
        val oldJson = getNovelFile(context, oldName)
        val newJson = getNovelFile(context, newName)
        if (oldJson.exists()) {
            oldJson.renameTo(newJson)
        }

        val oldDir = getImagesDir(context, oldName)
        val newDir = getImagesDir(context, newName)
        if (oldDir.exists()) {
            oldDir.renameTo(newDir)
        }

        // Update image paths inside JSON if they are absolute
        if (newJson.exists()) {
            try {
                val json = newJson.readText()
                val type = object : TypeToken<List<NovelPage>>() {}.type
                val pages: List<NovelPage> = gson.fromJson(json, type) ?: emptyList()
                val updatedPages = pages.map { page ->
                    if (page.imagePath != null && page.imagePath.contains("images_$oldName")) {
                        page.copy(imagePath = page.imagePath.replace("images_$oldName", "images_$newName"))
                    } else {
                        page
                    }
                }
                newJson.writeText(gson.toJson(updatedPages))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun exportNovelToZip(context: Context, novelName: String): File? {
        val pages = getPages(context, novelName)
        if (pages.isEmpty()) return null

        val cacheDir = File(context.cacheDir, "shared_novels")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val zipFile = File(cacheDir, "novel_${novelName}.zip")
        if (zipFile.exists()) zipFile.delete()

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                // 1. Add modified pages JSON with relative paths
                val modifiedPages = pages.map { page ->
                    if (page.imagePath != null) {
                        val fileName = File(page.imagePath).name
                        page.copy(imagePath = "images/$fileName")
                    } else {
                        page
                    }
                }
                val jsonContent = gson.toJson(modifiedPages)
                zos.putNextEntry(ZipEntry("novel_${novelName}.json"))
                zos.write(jsonContent.toByteArray())
                zos.closeEntry()

                // 2. Add images
                val imagesDir = getImagesDir(context, novelName)
                val imageFiles = imagesDir.listFiles()
                if (imageFiles != null) {
                    for (imgFile in imageFiles) {
                        if (imgFile.isFile) {
                            zos.putNextEntry(ZipEntry("images/${imgFile.name}"))
                            FileInputStream(imgFile).use { fis ->
                                fis.copyTo(zos)
                            }
                            zos.closeEntry()
                        }
                    }
                }
            }
            return zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun importNovelFromZip(context: Context, zipUri: Uri, existingNovels: List<String>): String? {
        var importedNovelName: String? = null
        var pagesJson: String? = null
        val tempImagesMap = mutableMapOf<String, ByteArray>()

        try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        if (name.startsWith("novel_") && name.endsWith(".json")) {
                            importedNovelName = name.substringAfter("novel_").substringBeforeLast(".json")
                            pagesJson = zis.bufferedReader().readText()
                        } else if (name.startsWith("images/") && !entry.isDirectory) {
                            val fileName = File(name).name
                            val bytes = zis.readBytes()
                            tempImagesMap[fileName] = bytes
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            val baseNovelName = importedNovelName ?: return null
            val rawJson = pagesJson ?: return null

            // Resolve name collision: append (1), (2), etc.
            var finalNovelName = baseNovelName
            var counter = 1
            while (existingNovels.contains(finalNovelName)) {
                finalNovelName = "$baseNovelName ($counter)"
                counter++
            }

            // Parse pages from raw json
            val type = object : TypeToken<List<NovelPage>>() {}.type
            val pages: List<NovelPage> = gson.fromJson(rawJson, type) ?: emptyList()

            // Save ALL images (including cover.jpg) from the zip to local app files directory
            val finalImagesDir = getImagesDir(context, finalNovelName)
            tempImagesMap.forEach { (fileName, bytes) ->
                val imageFile = File(finalImagesDir, fileName)
                FileOutputStream(imageFile).use { fos ->
                    fos.write(bytes)
                }
            }

            // Update page paths to point to the new local absolute paths
            val updatedPages = pages.map { page ->
                if (page.imagePath != null) {
                    val fileName = File(page.imagePath).name
                    val localFile = File(finalImagesDir, fileName)
                    if (localFile.exists()) {
                        page.copy(imagePath = localFile.absolutePath)
                    } else {
                        page.copy(imagePath = null)
                    }
                } else {
                    page
                }
            }

            // Write final pages json
            getNovelFile(context, finalNovelName).writeText(gson.toJson(updatedPages))

            return finalNovelName
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportNovelToPdf(context: Context, novelName: String): File? {
        val pages = getPages(context, novelName)
        if (pages.isEmpty()) return null

        val cacheDir = File(context.cacheDir, "shared_novels")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val pdfFile = File(cacheDir, "novel_${novelName}.pdf")
        if (pdfFile.exists()) pdfFile.delete()

        val pdfDocument = PdfDocument()

        // A4 page size in points (72 points per inch)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f
        val contentWidth = pageWidth - (margin * 2)
        val maxHeightPerPage = (pageHeight - margin * 2).toInt()

        val textPaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        var currentPdfPage: PdfDocument.Page? = null
        var currentY = margin

        fun finishCurrentPage() {
            currentPdfPage?.let { pdfDocument.finishPage(it) }
            currentPdfPage = null
        }

        fun startNewPage() {
            finishCurrentPage()
            val pageInfo =
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size).create()
            currentPdfPage = pdfDocument.startPage(pageInfo)
            currentY = margin
        }

        try {
            for (page in pages) {
                val isOnlyImage =
                    page.translatedText.isBlank() && (page.originalText.isNullOrBlank())

                // 1. Illustration (Image-only page)
                if (page.imagePath != null && isOnlyImage) {
                    val bitmap = BitmapFactory.decodeFile(page.imagePath)
                    if (bitmap != null) {
                        startNewPage()
                        val canvas = currentPdfPage!!.canvas

                        val scale = (contentWidth / bitmap.width).coerceAtMost((pageHeight - margin * 2) / bitmap.height)
                        val drawWidth = bitmap.width * scale
                        val drawHeight = bitmap.height * scale
                        val left = (pageWidth - drawWidth) / 2
                        val top = (pageHeight - drawHeight) / 2

                        canvas.drawBitmap(
                            bitmap,
                            null,
                            android.graphics.RectF(left, top, left + drawWidth, top + drawHeight),
                            Paint(Paint.FILTER_BITMAP_FLAG)
                        )
                        finishCurrentPage()
                        bitmap.recycle()
                    }
                }

                // 2. Text Content (Continuous flow)
                if (page.translatedText.isNotBlank()) {
                    val cleanText = page.translatedText
                        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                        .replace(Regex("\\*(.*?)\\*"), "$1")
                        .replace(Regex("#+\\s+"), "")

                    val staticLayout = StaticLayout.Builder.obtain(
                        cleanText, 0, cleanText.length, textPaint, contentWidth.toInt()
                    )
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.2f)
                        .setIncludePad(false)
                        .build()

                    var currentLine = 0
                    val totalLines = staticLayout.lineCount

                    while (currentLine < totalLines) {
                        if (currentPdfPage == null) {
                            startNewPage()
                        }

                        val canvas = currentPdfPage!!.canvas
                        val startLineInThisPage = currentLine
                        val startYInThisPage = currentY

                        // Determine how many lines fit on the rest of the current page
                        while (currentLine < totalLines) {
                            val lineBottom = staticLayout.getLineBottom(currentLine)
                            val lineTop = staticLayout.getLineTop(startLineInThisPage)
                            if (currentY + (lineBottom - lineTop) > pageHeight - margin) {
                                break
                            }
                            currentLine++
                        }

                        if (currentLine > startLineInThisPage) {
                            // Draw the lines that fit
                            val clipTop = staticLayout.getLineTop(startLineInThisPage)
                            val clipBottom = staticLayout.getLineBottom(currentLine - 1)
                            val heightDrawn = clipBottom - clipTop

                            canvas.save()
                            canvas.translate(margin, currentY)
                            canvas.clipRect(0f, 0f, contentWidth, heightDrawn.toFloat())
                            canvas.translate(0f, -clipTop.toFloat())
                            staticLayout.draw(canvas)
                            canvas.restore()

                            currentY += heightDrawn.toFloat()
                        }

                        // If we didn't finish all lines, it means we reached the bottom of the page
                        if (currentLine < totalLines) {
                            startNewPage()
                        }
                    }
                    
                    // Add a small paragraph spacing between NovelPages
                    currentY += 12f
                }
            }

            finishCurrentPage()
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }
}
