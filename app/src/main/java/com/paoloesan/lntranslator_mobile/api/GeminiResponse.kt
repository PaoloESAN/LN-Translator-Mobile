package com.paoloesan.lntranslator_mobile.api

data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    val content: ContentResponse? = null,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

data class ContentResponse(
    val parts: List<PartResponse>? = null,
    val role: String? = null
)

data class PartResponse(
    val text: String? = null
)

data class PromptFeedback(
    val blockReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

data class SafetyRating(
    val category: String? = null,
    val probability: String? = null
)