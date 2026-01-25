package com.paoloesan.lntranslator_mobile.translation.prompts

interface LanguagePrompt {
    val languageId: String
    val displayName: String

    fun getImageTranslationPrompt(userContext: String): String
    fun getTextTranslationPrompt(japaneseText: String, userContext: String): String
    fun getNoTextResponse(): String
}
