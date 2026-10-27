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
            userName = "Emma",
            greetingMessage = "Good Morning!",
            streakDays = 5,
            activePlants = listOf(
                PlantItem(
                    id = "1",
                    name = "Alphabet Fun",
                    species = "Letters & Sounds",
                    progress = 0.75f,
                    statusText = "Continue Playing",
                    badgeColorHex = "#2E8B57"
                )
            ),
            categories = listOf(
                CategoryItem(id = "cat_1", title = "Letters", itemCount = 12, iconName = "A"),
                CategoryItem(id = "cat_2", title = "Numbers", itemCount = 10, iconName = "123"),
                CategoryItem(id = "cat_3", title = "Animals", itemCount = 8, iconName = "🦁"),
                CategoryItem(id = "cat_4", title = "Shapes", itemCount = 6, iconName = "🔺"),
                CategoryItem(id = "cat_5", title = "Colours", itemCount = 9, iconName = "🎨"),
                CategoryItem(id = "cat_6", title = "Creativity", itemCount = 15, iconName = "✨")
            ),
            recentActivities = listOf(
                ActivityItem(id = "act_1", title = "Completed Alphabet Fun", timestamp = "1 hour ago", isCompleted = true),
                ActivityItem(id = "act_2", title = "Earned 50 Stars Reward", timestamp = "Yesterday", isCompleted = true)
            )
        )
        return flowOf(mockData)
    }
}
