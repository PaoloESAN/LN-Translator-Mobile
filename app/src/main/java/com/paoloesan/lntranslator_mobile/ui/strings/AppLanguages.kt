package com.paoloesan.lntranslator_mobile.ui.strings

import java.util.Locale

data class SupportedLanguage(
    val code: String,
    val nativeName: String,
)

object AppLanguages {
    const val SYSTEM = "system"
    const val ENGLISH = "en"
    const val SPANISH = "es"

    val supportedLanguages: List<SupportedLanguage> = listOf(
        SupportedLanguage(code = ENGLISH, nativeName = "English"),
        SupportedLanguage(code = SPANISH, nativeName = "Español"),
    )

    fun resolveLanguageCode(storedCode: String?): String {
        return when (storedCode?.lowercase()?.trim()) {
            ENGLISH, "english" -> ENGLISH
            SPANISH, "español", "espanol" -> SPANISH
            null, SYSTEM, "predeterminado del sistema", "system default" -> {
                val systemLang = Locale.getDefault().language
                if (systemLang.startsWith("es", ignoreCase = true)) SPANISH else ENGLISH
            }
            else -> {
                if (storedCode.startsWith("es", ignoreCase = true)) SPANISH else ENGLISH
            }
        }
    }
}
