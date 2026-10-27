package com.hathway.sproutelle.data.repository

import com.hathway.sproutelle.domain.model.CategoryItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.repository.ExplorerRepository
import com.hathway.sproutelle.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeRepositoryImpl(
    private val explorerRepository: ExplorerRepository
) : HomeRepository {

    override fun getHomeData(): Flow<HomeData> {
        return explorerRepository.getExplorerItems().map { explorerItems ->
            val realCategories = explorerItems.map { item ->
                CategoryItem(
                    id = item.id,
                    title = item.titleName,
                    itemCount = 0,
                    iconName = "",
                    imageUrl = item.imageUrl
                )
            }
            HomeData(
                userName = "", // Backend gap: no user profile table in Supabase schema
                greetingMessage = "Welcome to Sproutelle",
                streakDays = 0, // Backend gap: no user stats table in Supabase schema
                activePlants = emptyList(), // Backend gap: no plants table in Supabase schema
                categories = realCategories,
                recentActivities = emptyList() // Backend gap: no activities table in Supabase schema
            )
        }
    }
}
