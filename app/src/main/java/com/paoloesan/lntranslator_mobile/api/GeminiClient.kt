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
        - Si es una ilustración sin texto, responde "ILUSTRACIÓN SIN TEXTO".
        
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

        val base64Image = bitmapToBase64(bitmap)
        android.util.Log.d(
            "GeminiClient",
            "Imagen convertida a base64, longitud: ${base64Image.length}"
        )

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

        val maxRetries = 3
        var lastError = "Error desconocido"

        for (attempt in 1..maxRetries) {
            try {
                android.util.Log.d(
                    "GeminiClient",
                    "Enviando petición a Gemini (intento $attempt/$maxRetries)..."
                )

                val response = apiService.generateContent(
                    model = "gemini-3-flash-preview",
                    apiKey = apiKey,
                    request = request
                )

                android.util.Log.d("GeminiClient", "=== RESPUESTA RECIBIDA ===")
                android.util.Log.d(
                    "GeminiClient",
                    "Candidates count: ${response.candidates?.size ?: 0}"
                )

                val candidate = response.candidates?.firstOrNull()
                if (candidate == null) {
                    android.util.Log.e(
                        "GeminiClient",
                        "No hay candidates. Feedback: ${response.promptFeedback}"
                    )
                    return "Error: Sin candidates. Feedback: ${response.promptFeedback}"
                }

                android.util.Log.d("GeminiClient", "FinishReason: ${candidate.finishReason}")

                val content = candidate.content
                if (content == null) {
                    android.util.Log.e(
                        "GeminiClient",
                        "Content null. Razón: ${candidate.finishReason}"
                    )
                    return "Error: Content null. Razón: ${candidate.finishReason}"
                }

                android.util.Log.d("GeminiClient", "Parts count: ${content.parts?.size ?: 0}")
                content.parts?.forEachIndexed { index, part ->
                    android.util.Log.d(
                        "GeminiClient",
                        "Part[$index] text null?: ${part.text == null}"
                    )
                    android.util.Log.d(
                        "GeminiClient",
                        "Part[$index] text empty?: ${part.text?.isEmpty()}"
                    )
                    android.util.Log.d(
                        "GeminiClient",
                        "Part[$index] text length: ${part.text?.length ?: 0}"
                    )
                    if (!part.text.isNullOrEmpty()) {
                        android.util.Log.d(
                            "GeminiClient",
                            "Part[$index] preview: ${part.text.take(50)}"
                        )
                    }
                }

                val text = content.parts?.firstOrNull()?.text
                if (text.isNullOrEmpty()) {
                    android.util.Log.e("GeminiClient", "Texto vacío en respuesta")
                    android.util.Log.e("GeminiClient", "Content completo: $content")
                    return "Error: Texto vacío en respuesta"
                }

                android.util.Log.d("GeminiClient", "Texto recibido: ${text.take(100)}...")
                return text

            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("GeminiClient", "HTTP Error $code: $errorBody")

                if (code == 503 && attempt < maxRetries) {
                    val delayMs = attempt * 1500L
                    android.util.Log.w(
                        "GeminiClient",
                        "Modelo sobrecargado, reintentando en ${delayMs}ms..."
                    )
                    kotlinx.coroutines.delay(delayMs)
                    continue
                }

                lastError = when (code) {
                    503 -> "Modelo sobrecargado. Intenta de nuevo."
                    429 -> "Límite de peticiones. Espera un momento."
                    else -> "Error HTTP $code"
                }

            } catch (e: Exception) {
                android.util.Log.e("GeminiClient", "Exception: ${e.localizedMessage}")
                e.printStackTrace()
                lastError = "Error: ${e.localizedMessage}"
            }
        }

        return lastError
    }
}
