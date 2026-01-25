package com.paoloesan.lntranslator_mobile.translation.gemini

data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String? = null, val inline_data: InlineData? = null)
data class InlineData(val mime_type: String, val data: String)
