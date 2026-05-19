package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

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
}
