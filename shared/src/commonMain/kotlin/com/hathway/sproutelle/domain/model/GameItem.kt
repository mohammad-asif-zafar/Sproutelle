package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameItem(
    val assignmentId: String,
    val subcategoryId: String,
    val gameId: String,
    val name: String,
    val slug: String,
    val description: String?,
    val gameType: String,
    val isPublished: Boolean,
    val sortOrder: Int,
    val isActive: Boolean,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val difficulty: String? = null,
    val questionCount: Int? = null
)
