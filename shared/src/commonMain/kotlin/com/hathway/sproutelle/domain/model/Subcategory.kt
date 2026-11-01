package com.hathway.sproutelle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Subcategory(
    val id: String,
    val explorerId: String,
    val name: String,
    val imageAssetId: String?,
    val backgroundColor: String,
    val categoryId: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val imageUrl: String? = null,
    val signedAtTimestamp: Long = 0L
)
