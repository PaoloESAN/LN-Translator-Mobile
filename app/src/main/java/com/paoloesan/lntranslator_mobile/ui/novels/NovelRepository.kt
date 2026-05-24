package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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

            // Save images to local app files directory
            val finalImagesDir = getImagesDir(context, finalNovelName)
            val updatedPages = pages.map { page ->
                if (page.imagePath != null) {
                    val fileName = File(page.imagePath).name
                    val imageBytes = tempImagesMap[fileName]
                    if (imageBytes != null) {
                        val imageFile = File(finalImagesDir, fileName)
                        FileOutputStream(imageFile).use { fos ->
                            fos.write(imageBytes)
                        }
                        page.copy(imagePath = imageFile.absolutePath)
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
}
