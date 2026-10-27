package com.hathway.sproutelle.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaAssetEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("bucket_name") val bucketName: String? = null,
    @SerialName("file_path") val filePath: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("size_bytes") val sizeBytes: Long? = null,
    @SerialName("checksum_sha256") val checksumSha256: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
