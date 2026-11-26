package com.paoloesan.lntranslator_mobile.translation.gemini

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(val parts: List<Part>)
data class Part(val text: String? = null, val inline_data: InlineData? = null)
data class InlineData(val mime_type: String, val data: String)

data class GenerationConfig(
    val thinkingConfig: ThinkingConfig? = null
)

data class ThinkingConfig(
    val thinkingLevel: String? = null,
    val includeThoughts: Boolean? = null
)
