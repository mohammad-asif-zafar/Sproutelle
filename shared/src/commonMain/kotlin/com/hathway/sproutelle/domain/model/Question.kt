package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val gameId: String,
    val subcategoryId: String?,
    val text: String,
    val type: String,
    val instructions: String?,
    val sortOrder: Int,
    val isDraft: Boolean,
    val imageAssetId: String? = null,
    val audioAssetId: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val options: List<QuestionOption> = emptyList()
)
