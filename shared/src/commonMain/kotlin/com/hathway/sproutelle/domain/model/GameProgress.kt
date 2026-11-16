package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameProgress(
    val childId: String,
    val gameId: String,
    val levelId: String? = null,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val score: Int = 0,
    val completionStatus: String = "in_progress", // "in_progress", "completed", "passed"
    val timeSpentSeconds: Long = 0,
    val completionDate: String? = null,
    val bestScore: Int = 0
)
