package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeData(
    val userName: String,
    val greetingMessage: String,
    val streakDays: Int,
    val activePlants: List<PlantItem>,
    val categories: List<CategoryItem>,
    val recentActivities: List<ActivityItem>
)

@Serializable
data class PlantItem(
    val id: String,
    val name: String,
    val species: String,
    val progress: Float,
    val statusText: String,
    val badgeColorHex: String
)

@Serializable
data class CategoryItem(
    val id: String,
    val title: String,
    val itemCount: Int = 0,
    val iconName: String = "",
    val imageUrl: String? = null
)

@Serializable
data class ActivityItem(
    val id: String,
    val title: String,
    val timestamp: String,
    val isCompleted: Boolean
)
