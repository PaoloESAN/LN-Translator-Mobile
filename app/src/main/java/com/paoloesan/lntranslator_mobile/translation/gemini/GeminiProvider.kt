package com.paoloesan.lntranslator_mobile.translation.gemini

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paoloesan.lntranslator_mobile.translation.ImageTranslationRequest
import com.paoloesan.lntranslator_mobile.translation.TranslationProvider
import com.paoloesan.lntranslator_mobile.translation.TranslationResult
import com.paoloesan.lntranslator_mobile.translation.prompts.TranslationPrompts
import kotlinx.coroutines.delay
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiProvider(context: Context) : TranslationProvider {

    override val providerId: String = "gemini"
    override val displayName: String = "Google Gemini"

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val apiService: GeminiApiService

    private val apiKeys: List<String>
    private var currentKeyIndex: Int

    companion object {
        private const val TAG = "GeminiProvider"
        private const val MODEL_NAME = "gemini-3-flash-preview"
        private const val MAX_RETRIES = 3
    }

    init {
        apiKeys = loadApiKeys()
        currentKeyIndex = prefs.getInt("api_key_index", 0)
            .coerceIn(0, (apiKeys.size - 1).coerceAtLeast(0))

        apiService = createApiService()
    }

    private fun createApiService(): GeminiApiService {
        val responseLoggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            Log.d("GeminiHTTP", ">>> Enviando request a: ${request.url}")

            val response = chain.proceed(request)

            val responseBody = response.body
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            val responseString = buffer.readUtf8()

            Log.d("GeminiHTTP", "<<< Response Code: ${response.code}")
            Log.d("GeminiHTTP", "<<< Response Body RAW: $responseString")

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

        return retrofit.create(GeminiApiService::class.java)
    }

    override fun isConfigured(): Boolean {
        return apiKeys.isNotEmpty()
    }

    override suspend fun translateImage(request: ImageTranslationRequest): TranslationResult {
        if (!isConfigured()) {
            return TranslationResult.Error(
                message = "Configura tu API Key en ajustes",
                errorType = TranslationResult.ErrorType.NO_API_KEY
            )
        }

        val base64Image = bitmapToBase64(request.bitmap)
        Log.d(TAG, "Imagen convertida a base64, longitud: ${base64Image.length}")

        if (base64Image.length < 1000) {
            Log.e(TAG, "Base64 muy corto, imagen posiblemente corrupta")
            return TranslationResult.Error(
                message = "Error: Imagen corrupta o muy pequeña",
                errorType = TranslationResult.ErrorType.INVALID_IMAGE
            )
        }

        val prompt = TranslationPrompts.getImageTranslationPrompt(appContext, request.prompt)
        val geminiRequest = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(inline_data = InlineData("image/jpeg", base64Image)),
                        Part(text = prompt)
                    )
                )
            )
        )

        return executeWithRetryAndRotation(geminiRequest)
    }

    private suspend fun executeWithRetryAndRotation(request: GeminiRequest): TranslationResult {
        val totalKeysToTry = apiKeys.size.coerceAtMost(3)
        var lastError: TranslationResult.Error = TranslationResult.Error(
            message = "Error desconocido",
            errorType = TranslationResult.ErrorType.UNKNOWN
        )
        var keysTriedCount = 0

        while (keysTriedCount < totalKeysToTry) {
            val currentApiKey = getCurrentApiKey()
            Log.d(TAG, "Usando API Key ${currentKeyIndex + 1}/${apiKeys.size}")

            when (val result = attemptTranslation(request, currentApiKey)) {
                is TranslationResult.Success -> {
                    getNextApiKey()
                    return result
                }

                is TranslationResult.Error -> {
                    lastError = result

                    if (result.errorType == TranslationResult.ErrorType.RATE_LIMITED ||
                        result.errorType == TranslationResult.ErrorType.INVALID_API_KEY
                    ) {
                        if (apiKeys.size > 1) {
                            Log.w(TAG, "Error ${result.errorType}, rotando a siguiente key...")
                            getNextApiKey()
                            keysTriedCount++
                            continue
                        }
                    }

                    if (!result.isRetryable) {
                        return result
                    }
                }
            }

            keysTriedCount++
        }

        return lastError
    }

    private suspend fun attemptTranslation(
        request: GeminiRequest,
        apiKey: String
    ): TranslationResult {
        for (attempt in 1..MAX_RETRIES) {
            try {
                Log.d(TAG, "Enviando petición a Gemini (intento $attempt/$MAX_RETRIES)...")

                val response = apiService.generateContent(
                    model = MODEL_NAME,
                    apiKey = apiKey,
                    request = request
                )

                Log.d(TAG, "=== RESPUESTA RECIBIDA ===")
                Log.d(TAG, "Candidates count: ${response.candidates?.size ?: 0}")

                val candidate = response.candidates?.firstOrNull()
                if (candidate == null) {
                    Log.e(TAG, "No hay candidates. Feedback: ${response.promptFeedback}")
                    return TranslationResult.Error(
                        message = "Error: Sin candidates. Feedback: ${response.promptFeedback}",
                        errorType = TranslationResult.ErrorType.EMPTY_RESPONSE
                    )
                }

                val content = candidate.content
                if (content == null) {
                    Log.e(TAG, "Content null. Razón: ${candidate.finishReason}")
                    if (attempt < MAX_RETRIES) {
                        Log.w(TAG, "Reintentando por content null...")
                        delay(500L)
                        continue
                    }
                    return TranslationResult.Error(
                        message = "Error: Content null. Razón: ${candidate.finishReason}",
                        errorType = TranslationResult.ErrorType.EMPTY_RESPONSE,
                        isRetryable = true
                    )
                }

                val text = content.parts?.firstOrNull()?.text
                if (text.isNullOrEmpty()) {
                    Log.e(TAG, "=== TEXTO VACÍO ===")
                    Log.e(TAG, "Response JSON: ${Gson().toJson(response)}")

                    if (attempt < MAX_RETRIES) {
                        Log.w(TAG, "Reintentando por texto vacío...")
                        delay(500L)
                        continue
                    }
                    return TranslationResult.Error(
                        message = "Error: Texto vacío en respuesta",
                        errorType = TranslationResult.ErrorType.EMPTY_RESPONSE,
                        isRetryable = true
                    )
                }

                Log.d(TAG, "Texto recibido: ${text.take(100)}...")
                return TranslationResult.Success(text)

            } catch (e: HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "HTTP Error $code: $errorBody")

                return when (code) {
                    429 -> TranslationResult.Error(
                        message = "Límite de peticiones alcanzado",
                        errorType = TranslationResult.ErrorType.RATE_LIMITED,
                        isRetryable = true
                    )

                    403 -> TranslationResult.Error(
                        message = "API Key inválida",
                        errorType = TranslationResult.ErrorType.INVALID_API_KEY,
                        isRetryable = true
                    )

                    503 -> {
                        if (attempt < MAX_RETRIES) {
                            val delayMs = attempt * 1500L
                            Log.w(TAG, "Modelo sobrecargado, reintentando en ${delayMs}ms...")
                            delay(delayMs)
                            continue
                        }
                        TranslationResult.Error(
                            message = "Modelo sobrecargado. Intenta de nuevo.",
                            errorType = TranslationResult.ErrorType.MODEL_OVERLOADED,
                            isRetryable = true
                        )
                    }

                    else -> TranslationResult.Error(
                        message = "Error HTTP $code",
                        errorType = TranslationResult.ErrorType.NETWORK_ERROR
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.localizedMessage}")
                e.printStackTrace()

                return TranslationResult.Error(
                    message = "Error: ${e.localizedMessage}",
                    errorType = TranslationResult.ErrorType.NETWORK_ERROR
                )
            }
        }

        return TranslationResult.Error(
            message = "Error después de $MAX_RETRIES intentos",
            errorType = TranslationResult.ErrorType.UNKNOWN,
            isRetryable = false
        )
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun loadApiKeys(): List<String> {
        val jsonString = prefs.getString("api_keys_list", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val keys: List<String> = Gson().fromJson(jsonString, type)
                return keys.filter { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing API keys: ${e.message}")
            }
        }
        return emptyList()
    }

    private fun getNextApiKey(): String {
        if (apiKeys.isEmpty()) return ""
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        prefs.edit { putInt("api_key_index", currentKeyIndex) }
        Log.d(TAG, "Round robin: cambiando a API Key ${currentKeyIndex + 1}/${apiKeys.size}")
        return apiKeys[currentKeyIndex]
    }

    private fun getCurrentApiKey(): String {
        if (apiKeys.isEmpty()) return ""
        return apiKeys[currentKeyIndex.coerceIn(0, apiKeys.size - 1)]
    }
}