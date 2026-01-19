package com.paoloesan.lntranslator_mobile.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class GeminiClient(context: Context) {
    private val apiService: GeminiApiService
    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(GeminiApiService::class.java)
    }

    private fun getApiKey(): String {
        return prefs.getString("apikey_app", "") ?: ""
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun generateFromImage(prompt: String, bitmap: Bitmap): String? {
        val key = getApiKey()
        if (key.isEmpty()) return "Error: Configura tu API Key en ajustes"

        return try {
            val base64Image = bitmapToBase64(bitmap)
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(inline_data = InlineData("image/jpeg", base64Image)),
                            Part(text = prompt)
                        )
                    )
                )
            )

            val response = apiService.generateContent(
                model = "gemini-3-flash-preview",
                apiKey = key,
                request = request
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
