package com.paoloesan.lntranslator_mobile.translation.prompts

import android.content.Context
import com.paoloesan.lntranslator_mobile.translation.prompts.languages.EnglishPrompt
import com.paoloesan.lntranslator_mobile.translation.prompts.languages.SpanishPrompt

object TranslationPrompts {

    private const val PREF_NAME = "settings_prefs"
    private const val PREF_LANGUAGE = "idioma_app"
    private const val DEFAULT_LANGUAGE = "Español"

    val availableLanguages: Map<String, LanguagePrompt> = mapOf(
        "Español" to SpanishPrompt,
        "English" to EnglishPrompt
        // more languages
    )

    fun getAvailableLanguages(): List<String> = availableLanguages.keys.toList()

    fun getImageTranslationPrompt(context: Context, userContext: String): String {
        return getLanguagePrompt(context).getImageTranslationPrompt(userContext)
    }

    fun getTextTranslationPrompt(
        context: Context,
        japaneseText: String,
        userContext: String
    ): String {
        return getLanguagePrompt(context).getTextTranslationPrompt(japaneseText, userContext)
    }

    fun getNoTextResponse(context: Context): String {
        return getLanguagePrompt(context).getNoTextResponse()
    }

    fun getTargetLanguageName(context: Context): String {
        return getLanguagePrompt(context).displayName
    }

    private fun getLanguagePrompt(context: Context): LanguagePrompt {
        val selectedLanguage = getSelectedLanguage(context)
        return availableLanguages[selectedLanguage] ?: SpanishPrompt
    }

    private fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
}
