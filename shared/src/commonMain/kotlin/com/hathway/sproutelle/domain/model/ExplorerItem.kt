package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExplorerItem(
    val id: String,
    val titleName: String,
    val titleImageAssetId: String?,
    val backgroundColor: String,
    val categoryId: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val imageUrl: String? = null,
    val signedAtTimestamp: Long = 0L
)
