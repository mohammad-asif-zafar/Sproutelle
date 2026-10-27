package com.hathway.sproutelle.domain.model

data class ExplorerItem(
    val id: String,
    val titleName: String,
    val titleImageAssetId: String?,
    val backgroundColor: String,
    val categoryId: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val imageUrl: String? = null
)
