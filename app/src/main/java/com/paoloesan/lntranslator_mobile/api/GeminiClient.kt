package com.paoloesan.lntranslator_mobile.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiClient(context: Context) {
    private val apiService: GeminiApiService
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val apiKeys: List<String>
    private var currentKeyIndex: Int
    private val prompt = prefs.getString("prompt_app", "") ?: ""

    init {
        apiKeys = loadApiKeys()
        currentKeyIndex =
            prefs.getInt("api_key_index", 0).coerceIn(0, (apiKeys.size - 1).coerceAtLeast(0))

        val responseLoggingInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            android.util.Log.d("GeminiHTTP", ">>> Enviando request a: ${request.url}")

            val response = chain.proceed(request)

            val responseBody = response.body
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            val responseString = buffer.readUtf8()

            android.util.Log.d("GeminiHTTP", "<<< Response Code: ${response.code}")
            android.util.Log.d("GeminiHTTP", "<<< Response Body RAW: $responseString")

            response
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.MILLISECONDS))
            .retryOnConnectionFailure(true)
            .addInterceptor(responseLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(GeminiApiService::class.java)
    }

    private fun loadApiKeys(): List<String> {
        val jsonString = prefs.getString("api_keys_list", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val keys: List<String> = Gson().fromJson(jsonString, type)
                return keys.filter { it.isNotBlank() }
            } catch (e: Exception) {
                android.util.Log.e("GeminiClient", "Error parsing API keys: ${e.message}")
            }
        }
        return emptyList()
    }

    private fun getNextApiKey(): String {
        if (apiKeys.isEmpty()) return ""
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        prefs.edit { putInt("api_key_index", currentKeyIndex) }
        android.util.Log.d(
            "GeminiClient",
            "Round robin: cambiando a API Key ${currentKeyIndex + 1}/${apiKeys.size}"
        )
        return apiKeys[currentKeyIndex]
    }

    private fun getCurrentApiKey(): String {
        if (apiKeys.isEmpty()) return ""
        return apiKeys[currentKeyIndex.coerceIn(0, apiKeys.size - 1)]
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
        if (apiKeys.isEmpty()) return "Error: Configura tu API Key en ajustes"

        android.util.Log.d("GeminiClient", "=== VALIDANDO BITMAP ===")
        android.util.Log.d(
            "GeminiClient",
            "Bitmap width: ${bitmap.width}, height: ${bitmap.height}"
        )
        android.util.Log.d("GeminiClient", "Bitmap config: ${bitmap.config}")
        android.util.Log.d("GeminiClient", "Bitmap byteCount: ${bitmap.byteCount}")
        android.util.Log.d("GeminiClient", "Bitmap isRecycled: ${bitmap.isRecycled}")

        if (bitmap.width == 0 || bitmap.height == 0) {
            return "Error: Imagen capturada está vacía"
        }
        if (bitmap.isRecycled) {
            return "Error: Bitmap fue reciclado antes de procesar"
        }

        val base64Image = bitmapToBase64(bitmap)
        android.util.Log.d(
            "GeminiClient",
            "Imagen convertida a base64, longitud: ${base64Image.length}"
        )

        if (base64Image.length < 1000) {
            android.util.Log.e("GeminiClient", "Base64 muy corto, imagen posiblemente corrupta")
            return "Error: Imagen corrupta o muy pequeña"
        }

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

        android.util.Log.d("GeminiClient", "=== REQUEST ===")
        android.util.Log.d("GeminiClient", "Prompt length: ${systemPrompt(prompt).length}")
        android.util.Log.d("GeminiClient", "Image base64 length: ${base64Image.length}")
        android.util.Log.d("GeminiClient", "Parts count: ${request.contents.first().parts.size}")


        val maxRetries = 3
        val totalKeysToTry = apiKeys.size.coerceAtMost(3)
        var lastError = "Error desconocido"
        var keysTriedCount = 0

        while (keysTriedCount < totalKeysToTry) {
            val currentApiKey = getCurrentApiKey()
            android.util.Log.d(
                "GeminiClient",
                "Usando API Key ${currentKeyIndex + 1}/${apiKeys.size}"
            )

            for (attempt in 1..maxRetries) {
                try {
                    android.util.Log.d(
                        "GeminiClient",
                        "Enviando petición a Gemini (intento $attempt/$maxRetries, key ${currentKeyIndex + 1})..."
                    )

                    val response = apiService.generateContent(
                        model = "gemini-3-flash-preview",
                        apiKey = currentApiKey,
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
                        if (attempt < maxRetries) {
                            android.util.Log.w("GeminiClient", "Reintentando por content null...")
                            kotlinx.coroutines.delay(500L)
                            continue
                        }
                        if (apiKeys.size > 1) {
                            android.util.Log.w("GeminiClient", "Rotando key por content null...")
                            getNextApiKey()
                            keysTriedCount++
                            break
                        }
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
                        android.util.Log.e(
                            "GeminiClient",
                            "=== TEXTO VACÍO - RESPUESTA COMPLETA ==="
                        )
                        android.util.Log.e(
                            "GeminiClient",
                            "Response JSON: ${Gson().toJson(response)}"
                        )
                        android.util.Log.e("GeminiClient", "Candidate: ${Gson().toJson(candidate)}")
                        android.util.Log.e("GeminiClient", "Content: ${Gson().toJson(content)}")
                        android.util.Log.e(
                            "GeminiClient",
                            "FinishReason: ${candidate.finishReason}"
                        )
                        android.util.Log.e(
                            "GeminiClient",
                            "SafetyRatings: ${candidate.safetyRatings}"
                        )
                        android.util.Log.e(
                            "GeminiClient",
                            "PromptFeedback: ${response.promptFeedback}"
                        )

                        if (attempt < maxRetries) {
                            android.util.Log.w("GeminiClient", "Reintentando por texto vacío...")
                            kotlinx.coroutines.delay(500L)
                            continue
                        }
                        
                        if (apiKeys.size > 1) {
                            android.util.Log.w("GeminiClient", "Rotando key por texto vacío...")
                            getNextApiKey()
                            keysTriedCount++
                            break
                        }
                        return "Error: Texto vacío en respuesta"
                    }

                    getNextApiKey()
                    android.util.Log.d("GeminiClient", "Texto recibido: ${text.take(100)}...")
                    return text

                } catch (e: retrofit2.HttpException) {
                    val code = e.code()
                    val errorBody = e.response()?.errorBody()?.string()
                    android.util.Log.e("GeminiClient", "HTTP Error $code: $errorBody")

                    if ((code == 429 || code == 403) && apiKeys.size > 1) {
                        android.util.Log.w(
                            "GeminiClient",
                            "Error $code con key ${currentKeyIndex + 1}, rotando a siguiente key..."
                        )
                        getNextApiKey()
                        keysTriedCount++
                        break
                    }

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
                        429 -> "Límite de peticiones alcanzado en todas las keys."
                        403 -> "API Key inválida."
                        else -> "Error HTTP $code"
                    }


                    if (code != 429 && code != 403) {
                        return lastError
                    }

                } catch (e: Exception) {
                    android.util.Log.e("GeminiClient", "Exception: ${e.localizedMessage}")
                    e.printStackTrace()
                    lastError = "Error: ${e.localizedMessage}"
                }
            }
            keysTriedCount++
        }

        return lastError
    }
}
