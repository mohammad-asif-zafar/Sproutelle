package com.hathway.sproutelle.data.model

import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class GameLevelEntity(
    @SerialName("id") val id: String,
    @SerialName("game_id") val gameId: String,
    @SerialName("level_number") val levelNumber: Int = 1,
    @SerialName("name") val name: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("difficulty") val difficulty: String = "Medium",
    @SerialName("question_selection_rules") val questionSelectionRules: String? = null,
    @SerialName("min_score_to_unlock") val minScoreToUnlock: Int = 0,
    @SerialName("total_questions") val totalQuestions: Int = 5,
    @SerialName("completion_reward") val completionReward: String? = null,
    @SerialName("is_published") val isPublished: Boolean = true
) {
    fun toDomain(): GameLevel {
        return GameLevel(
            id = id,
            gameId = gameId,
            levelNumber = levelNumber,
            name = name.ifBlank { "Level $levelNumber" },
            description = description,
            difficulty = difficulty,
            questionSelectionRules = questionSelectionRules,
            minScoreToUnlock = minScoreToUnlock,
            totalQuestions = totalQuestions,
            completionReward = completionReward,
            isPublished = isPublished
        )
    }
}

@Serializable
data class GameConfigEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("instructions") val instructions: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("min_age") val minAge: Int = 3,
    @SerialName("max_age") val maxAge: Int = 8,
    @SerialName("difficulty") val difficulty: String = "Medium",
    @SerialName("total_questions") val totalQuestions: Int = 10,
    @SerialName("questions_per_session") val questionsPerSession: Int = 5,
    @SerialName("time_limit_seconds") val timeLimitSeconds: Int? = null,
    @SerialName("passing_score") val passingScore: Int = 80,
    @SerialName("points_per_correct") val pointsPerCorrect: Int = 10,
    @SerialName("enable_hints") val enableHints: Boolean = true,
    @SerialName("enable_sound") val enableSound: Boolean = true,
    @SerialName("enable_vibration") val enableVibration: Boolean = true,
    @SerialName("randomize_question_order") val randomizeQuestionOrder: Boolean = false,
    @SerialName("randomize_answer_order") val randomizeAnswerOrder: Boolean = false,
    @SerialName("status") val status: String = "published"
) {
    fun toDomain(): GameConfig {
        return GameConfig(
            gameId = id,
            name = name,
            description = description,
            instructions = instructions,
            iconUrl = iconUrl,
            coverImageUrl = coverImageUrl,
            minAge = minAge,
            maxAge = maxAge,
            difficulty = difficulty,
            totalQuestions = totalQuestions,
            questionsPerSession = questionsPerSession,
            timeLimitSeconds = timeLimitSeconds,
            passingScore = passingScore,
            pointsPerCorrect = pointsPerCorrect,
            enableHints = enableHints,
            enableSound = enableSound,
            enableVibration = enableVibration,
            randomizeQuestionOrder = randomizeQuestionOrder,
            randomizeAnswerOrder = randomizeAnswerOrder,
            status = status
        )
    }
}
