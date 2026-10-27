package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.data.model.ExplorerItemEntity
import com.hathway.sproutelle.data.model.MediaAssetEntity
import com.hathway.sproutelle.domain.model.ExplorerItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SignRequest(
    @SerialName("expiresIn") val expiresIn: Int,
    @SerialName("paths") val paths: List<String>
)

@Serializable
data class SignedUrlResponse(
    @SerialName("signedURL") val signedURL: String? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("path") val path: String? = null
)

interface ExplorerRemoteDataSource {
    suspend fun fetchExplorerItemsFromSupabase(): Result<List<ExplorerItem>>
}

class SupabaseExplorerRemoteDataSource : ExplorerRemoteDataSource {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun fetchExplorerItemsFromSupabase(): Result<List<ExplorerItem>> {
        return try {
            val explorerUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/homescreen_explorer?is_active=eq.true&order=sort_order.asc"
            val explorerHttpResponse: HttpResponse = client.get(explorerUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (explorerHttpResponse.status.value !in 200..299) {
                return Result.failure(Exception("Failed to fetch explorer items: HTTP ${explorerHttpResponse.status.value}"))
            }

            val explorerResponse: List<ExplorerItemEntity> = explorerHttpResponse.body()

            val assetIds = explorerResponse.mapNotNull { it.titleImageAssetId }.distinct()
            val mediaMap = if (assetIds.isNotEmpty()) {
                val idsParam = assetIds.joinToString(",")
                val mediaUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/media_assets?id=in.($idsParam)&select=id,name,bucket_name,file_path,mime_type,size_bytes,checksum_sha256,created_by,created_at"
                try {
                    val mediaHttpResponse: HttpResponse = client.get(mediaUrl) {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                        header(HttpHeaders.Accept, "application/json")
                    }
                    if (mediaHttpResponse.status.value in 200..299) {
                        val mediaList: List<MediaAssetEntity> = mediaHttpResponse.body()
                        mediaList.associateBy { it.id }
                    } else {
                        emptyMap()
                    }
                } catch (_: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            // Group file paths by bucket for batch signing
            val pathsByBucket = mutableMapOf<String, MutableList<String>>()
            explorerResponse.forEach { entity ->
                val asset = entity.titleImageAssetId?.let { mediaMap[it] }
                val bucketName = asset?.bucketName ?: "game-assets"
                val filePath = asset?.filePath
                if (!bucketName.isBlank() && !filePath.isNullOrBlank()) {
                    pathsByBucket.getOrPut(bucketName) { mutableListOf() }.add(filePath)
                }
            }

            val signedUrlMap = mutableMapOf<String, String>()
            val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

            for ((bucketName, paths) in pathsByBucket) {
                try {
                    val signUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/sign/$bucketName"
                    val signResponse: HttpResponse = client.post(signUrl) {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                        header(HttpHeaders.ContentType, "application/json")
                        setBody(SignRequest(expiresIn = 86400, paths = paths))
                    }
                    if (signResponse.status.value in 200..299) {
                        val bodyText = signResponse.bodyAsText()
                        val responses = try {
                            jsonParser.decodeFromString<List<SignedUrlResponse>>(bodyText)
                        } catch (_: Exception) {
                            listOf(jsonParser.decodeFromString<SignedUrlResponse>(bodyText))
                        }
                        responses.forEach { resp ->
                            val path = resp.path ?: resp.signedURL?.substringAfterLast("/")
                            val sUrl = resp.signedURL
                            if (!path.isNullOrBlank() && !sUrl.isNullOrBlank()) {
                                val fullUrl = when {
                                    sUrl.startsWith("http") -> sUrl
                                    sUrl.startsWith("/storage/v1") -> "${SupabaseConfig.SUPABASE_URL}$sUrl"
                                    sUrl.startsWith("/") -> "${SupabaseConfig.SUPABASE_URL}/storage/v1$sUrl"
                                    else -> "${SupabaseConfig.SUPABASE_URL}/storage/v1/$sUrl"
                                }
                                signedUrlMap[path] = fullUrl
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore signing errors for individual buckets
                }
            }

            val domainItems = explorerResponse.map { entity ->
                val asset = entity.titleImageAssetId?.let { mediaMap[it] }
                val filePath = asset?.filePath
                val imageUrl = filePath?.let { signedUrlMap[it] }
                entity.toDomain(asset, entity.titleImageAssetId, imageUrl)
            }

            Result.success(domainItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
