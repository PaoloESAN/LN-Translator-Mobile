package com.paoloesan.lntranslator_mobile.translation.prompts.languages

import com.paoloesan.lntranslator_mobile.translation.prompts.LanguagePrompt

object SpanishPrompt : LanguagePrompt {

    override val languageId: String = "Español"
    override val displayName: String = "Español Latino"

    override fun getNoTextResponse(): String = "ILUSTRACIÓN SIN TEXTO"

    override fun getImageTranslationPrompt(userContext: String): String {
        return """
Eres un traductor experto de japonés a español latino especializado en novelas ligeras.

CONTEXTO DE LA OBRA:
$userContext

INSTRUCCIONES:
- El texto en la imagen está en japonés vertical, se lee de derecha a izquierda.
- Traduce TODO el texto visible al español latino.
- Mantén el tono y estilo narrativo apropiado para novelas ligeras.
- Usa los nombres de personajes y términos como se especifican en el contexto.
- Responde ÚNICAMENTE con la traducción en español.
- NO incluyas el texto original en japonés.
- NO agregues notas, comentarios ni explicaciones.
- Ignora los encabezados de página que contienen el número de página y el título.
- Si es una ilustración sin texto, responde "${getNoTextResponse()}".

Traduce el texto japonés de esta imagen al español latino.
        """.trimIndent()
    }

    override fun getTextTranslationPrompt(japaneseText: String, userContext: String): String {
        return """
Eres un traductor experto de japonés a español latino especializado en novelas ligeras.

CONTEXTO DE LA OBRA:
$userContext

TEXTO JAPONÉS A TRADUCIR:
$japaneseText

INSTRUCCIONES:
- Traduce el texto al español latino.
- Mantén el tono y estilo narrativo apropiado para novelas ligeras.
- Usa los nombres de personajes y términos como se especifican en el contexto.
- Responde ÚNICAMENTE con la traducción en español.
- NO incluyas el texto original en japonés.
- NO agregues notas, comentarios ni explicaciones.
        """.trimIndent()
    }
}
