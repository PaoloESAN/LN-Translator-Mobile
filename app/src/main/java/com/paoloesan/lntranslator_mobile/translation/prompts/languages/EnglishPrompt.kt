package com.paoloesan.lntranslator_mobile.translation.prompts.languages

import com.paoloesan.lntranslator_mobile.translation.prompts.LanguagePrompt

object EnglishPrompt : LanguagePrompt {

    override val languageId: String = "English"
    override val displayName: String = "English"

    override fun getNoTextResponse(): String = "ILLUSTRATION WITHOUT TEXT"

    override fun getImageTranslationPrompt(userContext: String): String {
        return """
You are an expert Japanese to English translator specializing in light novels.

WORK CONTEXT:
$userContext

INSTRUCTIONS:
- The text in the image is in vertical Japanese, read from right to left.
- Translate ALL visible text to English.
- Maintain the appropriate narrative tone and style for light novels.
- Use character names and terms as specified in the context.
- Respond ONLY with the English translation.
- Do NOT include the original Japanese text.
- Do NOT add notes, comments, or explanations.
- Ignore page headers containing page numbers and titles.
- If it's an illustration without text, respond "${getNoTextResponse()}".

Translate the Japanese text in this image to English.
        """.trimIndent()
    }

    override fun getTextTranslationPrompt(japaneseText: String, userContext: String): String {
        return """
You are an expert Japanese to English translator specializing in light novels.

WORK CONTEXT:
$userContext

JAPANESE TEXT TO TRANSLATE:
$japaneseText

INSTRUCTIONS:
- Translate the text to English.
- Maintain the appropriate narrative tone and style for light novels.
- Use character names and terms as specified in the context.
- Respond ONLY with the English translation.
- Do NOT include the original Japanese text.
- Do NOT add notes, comments, or explanations.
        """.trimIndent()
    }
}
