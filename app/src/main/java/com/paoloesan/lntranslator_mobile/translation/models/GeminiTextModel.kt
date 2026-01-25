package com.paoloesan.lntranslator_mobile.translation.models

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paoloesan.lntranslator_mobile.translation.prompts.TranslationPrompts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiTextModel(context: Context) : TextTranslationModel {

    override val modelId: String = "gemini"
    override val displayName: String = "Gemini"

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKeys: List<String> by lazy { loadApiKeys() }
    private var currentKeyIndex: Int = 0

    companion object {
        private const val TAG = "GeminiTextModel"
        private const val GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent"
    }

    override fun isConfigured(): Boolean {
        return apiKeys.isNotEmpty()
    }

    override suspend fun translateText(
        japaneseText: String,
        contextPrompt: String
    ): TextTranslationResult {
        if (!isConfigured()) {
            return TextTranslationResult.Error("Configura tu API Key de Gemini en ajustes")
        }

        val prompt = TranslationPrompts.getTextTranslationPrompt(appContext, japaneseText, contextPrompt)

        val requestBody = """
        {
            "contents": [{
                "parts": [{
                    "text": ${Gson().toJson(prompt)}
                }]
            }]
        }
        """.trimIndent()

        return withContext(Dispatchers.IO) {
            var lastError: TextTranslationResult.Error? = null
            val keysToTry = apiKeys.size.coerceAtMost(3)

            for (attempt in 0 until keysToTry) {
                val apiKey = getCurrentApiKey()
                Log.d(TAG, "Usando API Key ${currentKeyIndex + 1}/${apiKeys.size}")

                try {
                    val request = Request.Builder()
                        .url("$GEMINI_URL?key=$apiKey")
                        .post(requestBody.toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val translatedText = parseGeminiResponse(responseBody)
                        if (translatedText != null) {
                            rotateApiKey()
                            return@withContext TextTranslationResult.Success(translatedText)
                        } else {
                            lastError = TextTranslationResult.Error("Respuesta vacía de Gemini")
                        }
                    } else {
                        Log.e(TAG, "Error HTTP ${response.code}: $responseBody")

                        when (response.code) {
                            429, 403 -> {
                                rotateApiKey()
                                lastError = TextTranslationResult.Error(
                                    message = if (response.code == 429) "Rate limit" else "API Key inválida",
                                    isRetryable = true
                                )
                                continue
                            }

                            else -> {
                                lastError =
                                    TextTranslationResult.Error("Error HTTP ${response.code}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Excepción: ${e.localizedMessage}")
                    lastError = TextTranslationResult.Error("Error de red: ${e.localizedMessage}")
                }

                rotateApiKey()
            }

            lastError ?: TextTranslationResult.Error("Error desconocido")
        }
    }

    private fun parseGeminiResponse(responseBody: String): String? {
        return try {
            val json = Gson().fromJson(responseBody, Map::class.java)
            val candidates = json["candidates"] as? List<*>
            val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.firstOrNull() as? Map<*, *>
            val text = firstPart?.get("text") as? String
            text?.trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta: ${e.localizedMessage}")
            null
        }
    }

    private fun loadApiKeys(): List<String> {
        val jsonString = prefs.getString("api_keys_list", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val keys: List<String> = Gson().fromJson(jsonString, type)
                return keys.filter { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando API keys: ${e.message}")
            }
        }
        return emptyList()
    }

    private fun getCurrentApiKey(): String {
        if (apiKeys.isEmpty()) return ""
        currentKeyIndex = currentKeyIndex.coerceIn(0, apiKeys.size - 1)
        return apiKeys[currentKeyIndex]
    }

    private fun rotateApiKey() {
        if (apiKeys.isNotEmpty()) {
            currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        }
    }
}
