package com.hathway.sproutelle.data.model

import com.hathway.sproutelle.domain.model.Subcategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubcategoryEntity(
    @SerialName("id") val id: String,
    @SerialName("explorer_id") val explorerId: String,
    @SerialName("name") val name: String,
    @SerialName("image_asset_id") val imageAssetId: String? = null,
    @SerialName("background_color") val backgroundColor: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
) {
    fun toDomain(signedUrl: String?, signedAt: Long = 0L): Subcategory {
        return Subcategory(
            id = id,
            explorerId = explorerId,
            name = name,
            imageAssetId = imageAssetId,
            backgroundColor = backgroundColor ?: "#E8F8EE",
            categoryId = categoryId,
            sortOrder = sortOrder,
            isActive = isActive,
            imageUrl = signedUrl,
            signedAtTimestamp = signedAt
        )
    }
}
