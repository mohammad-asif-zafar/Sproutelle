package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val gameId: String,
    val subcategoryId: String?,
    val levelId: String? = null,
    val text: String,
    val type: String, // "find_correct_picture", "identify_object", "match_picture_word", "find_different_picture", "category_identification", "image_selection"
    val instructions: String? = null,
    val sortOrder: Int = 0,
    val isDraft: Boolean = false,
    val imageAssetId: String? = null,
    val audioAssetId: String? = null,
    val questionIllustrationAssetId: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val questionIllustrationUrl: String? = null,
    val hint: String? = null,
    val explanation: String? = null,
    val points: Int = 10,
    val difficulty: String = "Medium",
    val options: List<QuestionOption> = emptyList()
)
