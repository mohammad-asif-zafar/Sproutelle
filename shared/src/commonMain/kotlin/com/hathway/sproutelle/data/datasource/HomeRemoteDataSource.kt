package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.CategoryItem
import com.hathway.sproutelle.domain.model.HomeData

interface HomeRemoteDataSource {
    suspend fun fetchHomeDataFromSupabase(): Result<HomeData>
}

class SupabaseHomeRemoteDataSource(
    private val explorerRemoteDataSource: ExplorerRemoteDataSource = SupabaseExplorerRemoteDataSource()
) : HomeRemoteDataSource {
    override suspend fun fetchHomeDataFromSupabase(): Result<HomeData> {
        val explorerResult = explorerRemoteDataSource.fetchExplorerItemsFromSupabase()
        return explorerResult.map { items ->
            val realCategories = items.filter { it.isActive }.sortedBy { it.sortOrder }.map { item ->
                CategoryItem(
                    id = item.id,
                    title = item.titleName,
                    itemCount = 1,
                    iconName = item.titleImageAssetId ?: ""
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
