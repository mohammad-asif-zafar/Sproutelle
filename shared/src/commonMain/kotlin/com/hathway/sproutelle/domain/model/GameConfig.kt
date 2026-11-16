package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val gameId: String,
    val name: String,
    val description: String? = null,
    val instructions: String? = null,
    val iconUrl: String? = null,
    val coverImageUrl: String? = null,
    val minAge: Int = 3,
    val maxAge: Int = 8,
    val difficulty: String = "Medium",
    val totalQuestions: Int = 10,
    val questionsPerSession: Int = 5,
    val timeLimitSeconds: Int? = null,
    val passingScore: Int = 80,
    val pointsPerCorrect: Int = 10,
    val enableHints: Boolean = true,
    val enableSound: Boolean = true,
    val enableVibration: Boolean = true,
    val randomizeQuestionOrder: Boolean = false,
    val randomizeAnswerOrder: Boolean = false,
    val status: String = "published"
)

@Serializable
data class GameLevel(
    val id: String,
    val gameId: String,
    val levelNumber: Int,
    val name: String,
    val description: String? = null,
    val difficulty: String = "Medium",
    val questionSelectionRules: String? = null,
    val minScoreToUnlock: Int = 0,
    val totalQuestions: Int = 5,
    val completionReward: String? = null,
    val isPublished: Boolean = true
)
