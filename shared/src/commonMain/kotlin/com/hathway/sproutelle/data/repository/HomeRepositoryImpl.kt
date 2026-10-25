package com.hathway.sproutelle.data.repository

import com.hathway.sproutelle.domain.model.ActivityItem
import com.hathway.sproutelle.domain.model.CategoryItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.model.PlantItem
import com.hathway.sproutelle.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class HomeRepositoryImpl : HomeRepository {

    override fun getHomeData(): Flow<HomeData> {
        val mockData = HomeData(
            userName = "Sarah",
            greetingMessage = "Welcome back! Your garden is thriving today 🌱",
            streakDays = 14,
            activePlants = listOf(
                PlantItem(
                    id = "1",
                    name = "Monstera Deliciosa",
                    species = "Swiss Cheese Plant",
                    progress = 0.85f,
                    statusText = "Watering due tomorrow",
                    badgeColorHex = "#2E8B57"
                ),
                PlantItem(
                    id = "2",
                    name = "Fiddle Leaf Fig",
                    species = "Ficus lyrata",
                    progress = 0.60f,
                    statusText = "Needs bright indirect light",
                    badgeColorHex = "#F47B52"
                ),
                PlantItem(
                    id = "3",
                    name = "Golden Pothos",
                    species = "Epipremnum aureum",
                    progress = 0.95f,
                    statusText = "Thriving well",
                    badgeColorHex = "#57A9DE"
                )
            ),
            categories = listOf(
                CategoryItem(id = "cat_1", title = "Indoor Plants", itemCount = 8, iconName = "potted_plant"),
                CategoryItem(id = "cat_2", title = "Succulents", itemCount = 5, iconName = "cactus"),
                CategoryItem(id = "cat_3", title = "Herbs", itemCount = 4, iconName = "spa"),
                CategoryItem(id = "cat_4", title = "Outdoor Garden", itemCount = 12, iconName = "nature")
            ),
            recentActivities = listOf(
                ActivityItem(id = "act_1", title = "Watered Monstera Deliciosa", timestamp = "2 hours ago", isCompleted = true),
                ActivityItem(id = "act_2", title = "Added Fertilizer to Fiddle Leaf", timestamp = "Yesterday", isCompleted = true),
                ActivityItem(id = "act_3", title = "Pruned Golden Pothos leaves", timestamp = "3 days ago", isCompleted = true)
            )
        )
        return flowOf(mockData)
    }
}
