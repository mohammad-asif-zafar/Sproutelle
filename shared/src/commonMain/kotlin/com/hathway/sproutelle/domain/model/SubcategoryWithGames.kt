package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SubcategoryWithGames(
    val subcategory: Subcategory,
    val games: List<GameItem>
)
