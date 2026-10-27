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
            println("SproutelleDiagnostic: Attempting request to Supabase explorer")

            val explorerHttpResponse: HttpResponse = client.get(explorerUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }
            val explorerResponse: List<ExplorerItemEntity> = explorerHttpResponse.body()
            println("SproutelleDiagnostic: Fetched ${explorerResponse.size} active explorer records from Supabase")

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
                    val mediaList: List<MediaAssetEntity> = mediaHttpResponse.body()
                    println("SproutelleDiagnostic: Fetched ${mediaList.size} matching media assets")
                    mediaList.associateBy { it.id }
                } catch (e: Exception) {
                    println("SproutelleDiagnostic: media_assets fetch failed: ${e.message}")
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

            var representativeTested = false
            val domainItems = explorerResponse.map { entity ->
                val asset = entity.titleImageAssetId?.let { mediaMap[it] }
                val bucketName = asset?.bucketName ?: "game-assets"
                val filePath = asset?.filePath

                val imageUrl = if (!bucketName.isBlank() && !filePath.isNullOrBlank()) {
                    try {
                        val signUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/sign/$bucketName"
                        val signResponse: HttpResponse = client.post(signUrl) {
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                            header(HttpHeaders.ContentType, "application/json")
                            setBody(SignRequest(expiresIn = 86400, paths = listOf(filePath)))
                        }
                        val status = signResponse.status.value
                        val bodyText = signResponse.bodyAsText()
                        println("SproutelleDiagnostic: Sign response for '${entity.titleName}' ($filePath) -> Status: $status, Body: $bodyText")
                        if (status in 200..299) {
                            val sUrl = try {
                                val parsedList = jsonParser.decodeFromString<List<SignedUrlResponse>>(bodyText)
                                parsedList.firstOrNull()?.signedURL
                            } catch (_: Exception) {
                                val parsedObj = jsonParser.decodeFromString<SignedUrlResponse>(bodyText)
                                parsedObj.signedURL
                            }
                            if (!sUrl.isNullOrBlank()) {
                                val fullUrl = when {
                                    sUrl.startsWith("http") -> sUrl
                                    sUrl.startsWith("/storage/v1") -> "${SupabaseConfig.SUPABASE_URL}$sUrl"
                                    sUrl.startsWith("/") -> "${SupabaseConfig.SUPABASE_URL}/storage/v1$sUrl"
                                    else -> "${SupabaseConfig.SUPABASE_URL}/storage/v1/$sUrl"
                                }
                                fullUrl
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        println("SproutelleDiagnostic: Sign exception for '${entity.titleName}': ${e::class.simpleName} - ${e.message}")
                        null
                    }
                } else {
                    null
                }

                println("SproutelleDiagnostic: Category [ID: ${entity.id}, Title: '${entity.titleName}', SortOrder: ${entity.sortOrder}, Active: ${entity.isActive}] -> titleImageAssetId: ${entity.titleImageAssetId}, AssetFound: ${asset != null}, Bucket: $bucketName, FilePath: $filePath, ImageUrl: ${imageUrl?.let { if (it.length > 60) "${it.take(57)}..." else it }}")

                // Test representative image request once
                if (!representativeTested && imageUrl != null) {
                    representativeTested = true
                    try {
                        val imgResponse: HttpResponse = client.get(imageUrl) {
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                        }
                        val status = imgResponse.status.value
                        val contentType = imgResponse.headers[HttpHeaders.ContentType] ?: "unknown"
                        val bodyText = if (status != 200) imgResponse.bodyAsText() else ""
                        println("SproutelleDiagnostic: Representative image request for '${entity.titleName}' -> Status: $status, Content-Type: $contentType, Body: $bodyText")
                    } catch (e: Exception) {
                        println("SproutelleDiagnostic: Representative image request exception for '${entity.titleName}': ${e::class.simpleName} - ${e.message}")
                    }
                }

                entity.toDomain(asset, entity.titleImageAssetId, imageUrl)
            }

            Result.success(domainItems)
        } catch (e: Exception) {
            println("SproutelleDiagnostic: Supabase request exception: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }
}
