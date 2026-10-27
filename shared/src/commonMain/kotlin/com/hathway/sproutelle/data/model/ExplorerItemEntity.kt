package com.hathway.sproutelle.data.model

import com.hathway.sproutelle.domain.model.ExplorerItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExplorerItemEntity(
    @SerialName("id") val id: String,
    @SerialName("title_name") val titleName: String,
    @SerialName("title_image_asset_id") val titleImageAssetId: String? = null,
    @SerialName("background_color") val backgroundColor: String = "#FFF0F3",
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
) {
    fun toDomain(mediaAsset: MediaAssetEntity?, imageAssetId: String?, signedUrl: String?): ExplorerItem {
        return ExplorerItem(
            id = id,
            titleName = titleName,
            titleImageAssetId = imageAssetId,
            backgroundColor = backgroundColor,
            categoryId = categoryId,
            sortOrder = sortOrder,
            isActive = isActive,
            imageUrl = signedUrl
        )
    }
}
