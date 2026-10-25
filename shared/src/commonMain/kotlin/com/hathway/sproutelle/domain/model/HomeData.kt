package com.hathway.sproutelle.domain.model

data class HomeData(
    val userName: String,
    val greetingMessage: String,
    val streakDays: Int,
    val activePlants: List<PlantItem>,
    val categories: List<CategoryItem>,
    val recentActivities: List<ActivityItem>
)

data class PlantItem(
    val id: String,
    val name: String,
    val species: String,
    val progress: Float,
    val statusText: String,
    val badgeColorHex: String
)

data class CategoryItem(
    val id: String,
    val title: String,
    val itemCount: Int,
    val iconName: String
)

data class ActivityItem(
    val id: String,
    val title: String,
    val timestamp: String,
    val isCompleted: Boolean
)
