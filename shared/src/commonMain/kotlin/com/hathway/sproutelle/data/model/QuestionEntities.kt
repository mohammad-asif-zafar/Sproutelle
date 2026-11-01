package com.hathway.sproutelle.data.model

import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class QuestionEntity(
    @SerialName("id") val id: String,
    @SerialName("game_id") val gameId: String,
    @SerialName("subcategory_id") val subcategoryId: String? = null,
    @SerialName("question_text") val questionText: String = "",
    @SerialName("question_type") val questionType: String = "general",
    @SerialName("instructions") val instructions: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_draft") val isDraft: Boolean = false,
    @SerialName("image_asset_id") val imageAssetId: String? = null,
    @SerialName("audio_asset_id") val audioAssetId: String? = null,
    @SerialName("config") val config: JsonElement? = null
) {
    fun toDomain(
        imageUrl: String? = null,
        audioUrl: String? = null,
        options: List<QuestionOption> = emptyList()
    ): Question {
        return Question(
            id = id,
            gameId = gameId,
            subcategoryId = subcategoryId,
            text = questionText,
            type = questionType,
            instructions = instructions,
            sortOrder = sortOrder,
            isDraft = isDraft,
            imageAssetId = imageAssetId,
            audioAssetId = audioAssetId,
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            options = options
        )
    }
}

@Serializable
data class QuestionOptionEntity(
    @SerialName("id") val id: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("option_text") val optionText: String = "",
    @SerialName("is_correct") val isCorrect: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("image_asset_id") val imageAssetId: String? = null,
    @SerialName("audio_asset_id") val audioAssetId: String? = null
) {
    fun toDomain(imageUrl: String? = null, audioUrl: String? = null): QuestionOption {
        return QuestionOption(
            id = id,
            questionId = questionId,
            text = optionText,
            isCorrect = isCorrect,
            sortOrder = sortOrder,
            imageAssetId = imageAssetId,
            audioAssetId = audioAssetId,
            imageUrl = imageUrl,
            audioUrl = audioUrl
        )
    }
}
