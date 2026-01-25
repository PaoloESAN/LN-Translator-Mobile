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
import com.paoloesan.lntranslator_mobile.ui.strings.StringsProvider
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
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

    private val strings: UiStrings
        get() = StringsProvider.getStrings(prefs.getString("idioma_app", null))

    init {
        apiKeys = loadApiKeys()
        currentKeyIndex = prefs.getInt("api_key_index", 0)
            .coerceIn(0, (apiKeys.size - 1).coerceAtLeast(0))

        apiService = createApiService()
    }

    private fun createApiService(): GeminiApiService {
        val responseLoggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
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
                message = strings.errorNoApiKey(displayName),
                errorType = TranslationResult.ErrorType.NO_API_KEY
            )
        }

        val base64Image = bitmapToBase64(request.bitmap)

        if (base64Image.length < 1000) {
            return TranslationResult.Error(
                message = strings.errorImageCorrupt,
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
            message = strings.errorUnknown,
            errorType = TranslationResult.ErrorType.UNKNOWN
        )
        var keysTriedCount = 0

        while (keysTriedCount < totalKeysToTry) {
            val currentApiKey = getCurrentApiKey()

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
                val response = apiService.generateContent(
                    model = MODEL_NAME,
                    apiKey = apiKey,
                    request = request
                )

                val candidate = response.candidates?.firstOrNull()
                if (candidate == null) {
                    return TranslationResult.Error(
                        message = strings.errorEmptyResponse,
                        errorType = TranslationResult.ErrorType.EMPTY_RESPONSE
                    )
                }

                val content = candidate.content
                if (content == null) {
                    if (attempt < MAX_RETRIES) {
                        delay(500L)
                        continue
                    }
                    return TranslationResult.Error(
                        message = strings.errorEmptyResponse,
                        errorType = TranslationResult.ErrorType.EMPTY_RESPONSE,
                        isRetryable = true
                    )
                }

                val text = content.parts?.firstOrNull()?.text
                if (text.isNullOrEmpty()) {
                    if (attempt < MAX_RETRIES) {
                        delay(500L)
                        continue
                    }
                    return TranslationResult.Error(
                        message = strings.errorEmptyResponse,
                        errorType = TranslationResult.ErrorType.EMPTY_RESPONSE,
                        isRetryable = true
                    )
                }

                return TranslationResult.Success(text)

            } catch (e: HttpException) {
                val code = e.code()
                return when (code) {
                    429 -> TranslationResult.Error(
                        message = strings.errorRateLimited,
                        errorType = TranslationResult.ErrorType.RATE_LIMITED,
                        isRetryable = true
                    )

                    403 -> TranslationResult.Error(
                        message = strings.errorInvalidApiKey,
                        errorType = TranslationResult.ErrorType.INVALID_API_KEY,
                        isRetryable = true
                    )

                    503 -> {
                        if (attempt < MAX_RETRIES) {
                            val delayMs = attempt * 1500L
                            delay(delayMs)
                            continue
                        }
                        TranslationResult.Error(
                            message = strings.errorModelOverloaded,
                            errorType = TranslationResult.ErrorType.MODEL_OVERLOADED,
                            isRetryable = true
                        )
                    }

                    else -> TranslationResult.Error(
                        message = "HTTP $code",
                        errorType = TranslationResult.ErrorType.NETWORK_ERROR
                    )
                }
            } catch (e: Exception) {
                return TranslationResult.Error(
                    message = "${strings.errorUnknown}: ${e.localizedMessage}",
                    errorType = TranslationResult.ErrorType.NETWORK_ERROR
                )
            }
        }

        return TranslationResult.Error(
            message = strings.errorUnknown,
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
        return apiKeys[currentKeyIndex]
    }

    private fun getCurrentApiKey(): String {
        if (apiKeys.isEmpty()) return ""
        return apiKeys[currentKeyIndex.coerceIn(0, apiKeys.size - 1)]
    }
}