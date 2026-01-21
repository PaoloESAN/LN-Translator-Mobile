package com.paoloesan.lntranslator_mobile.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiClient(context: Context) {
    private val apiService: GeminiApiService
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val apiKey = prefs.getString("apikey_app", "") ?: ""
    private val prompt = prefs.getString("prompt_app", "") ?: ""

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(GeminiApiService::class.java)
    }

    private fun systemPrompt(userContextPrompt: String): String {
        return """
        Eres un traductor experto de japonés a español latino especializado en novelas ligeras.

        CONTEXTO DE LA OBRA:
        $userContextPrompt

        INSTRUCCIONES:
        - El texto en la imagen está en japonés vertical, se lee de derecha a izquierda.
        - Traduce TODO el texto visible al español latino.
        - Mantén el tono y estilo narrativo apropiado para novelas ligeras.
        - Usa los nombres de personajes y términos como se especifican en el contexto.
        - Responde ÚNICAMENTE con la traducción en español.
        - NO incluyas el texto original en japonés.
        - NO agregues notas, comentarios ni explicaciones.
        - Ignora los encabezados de página que contienen el número de página y el título
        
        Traduce el texto japonés de esta imagen al español latino.
        """.trimIndent()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun generateFromImage(bitmap: Bitmap): String {
        if (apiKey.isEmpty()) return "Error: Configura tu API Key en ajustes"

        return try {
            val base64Image = bitmapToBase64(bitmap)
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(inline_data = InlineData("image/jpeg", base64Image)),
                            Part(text = systemPrompt(prompt))
                        )
                    )
                )
            )

            val response = apiService.generateContent(
                model = "gemini-3-flash-preview",
                apiKey = apiKey,
                request = request
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Error: Respuesta vacía del modelo"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.localizedMessage}"
        }
    }
}
