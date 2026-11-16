package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionOption(
    val id: String,
    val questionId: String,
    val text: String,
    val isCorrect: Boolean,
    val sortOrder: Int,
    val imageAssetId: String? = null,
    val audioAssetId: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val explanation: String? = null
)
