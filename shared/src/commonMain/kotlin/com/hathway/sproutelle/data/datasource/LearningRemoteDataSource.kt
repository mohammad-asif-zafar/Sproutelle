package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.data.model.GameEntity
import com.hathway.sproutelle.data.model.MediaAssetEntity
import com.hathway.sproutelle.data.model.QuestionEntity
import com.hathway.sproutelle.data.model.QuestionOptionEntity
import com.hathway.sproutelle.data.model.SubcategoryEntity
import com.hathway.sproutelle.data.model.SubcategoryGameEntity
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
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
import kotlinx.serialization.json.Json

interface LearningRemoteDataSource {
    suspend fun fetchSubcategories(explorerId: String): Result<List<Subcategory>>
    suspend fun fetchGamesForSubcategory(subcategoryId: String): Result<List<GameItem>>
    suspend fun fetchQuestions(subcategoryId: String, gameId: String): Result<List<Question>>
}

class SupabaseLearningRemoteDataSource(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) : LearningRemoteDataSource {

    override suspend fun fetchSubcategories(explorerId: String): Result<List<Subcategory>> {
        return try {
            val subcategoriesUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/homescreen_explorer_subcategories?explorer_id=eq.$explorerId&is_active=eq.true&order=sort_order.asc"
            val response: HttpResponse = client.get(subcategoriesUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (response.status.value !in 200..299) {
                return Result.failure(Exception("Failed to fetch subcategories: HTTP ${response.status.value}"))
            }

            val subcategoryEntities: List<SubcategoryEntity> = response.body()

            val assetIds = subcategoryEntities.mapNotNull { it.imageAssetId }.distinct()
            val mediaMap = if (assetIds.isNotEmpty()) {
                val idsParam = assetIds.joinToString(",")
                val mediaUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/media_assets?id=in.($idsParam)&select=id,name,bucket_name,file_path,mime_type,size_bytes,checksum_sha256,created_by,created_at"
                try {
                    val mediaResponse: HttpResponse = client.get(mediaUrl) {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                        header(HttpHeaders.Accept, "application/json")
                    }
                    if (mediaResponse.status.value in 200..299) {
                        val mediaList: List<MediaAssetEntity> = mediaResponse.body()
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

            val pathsByBucket = mutableMapOf<String, MutableList<String>>()
            subcategoryEntities.forEach { entity ->
                val asset = entity.imageAssetId?.let { mediaMap[it] }
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
                    // Ignore signing errors
                }
            }

            val domainItems = subcategoryEntities.map { entity ->
                val asset = entity.imageAssetId?.let { mediaMap[it] }
                val filePath = asset?.filePath
                val imageUrl = filePath?.let { signedUrlMap[it] }
                entity.toDomain(imageUrl)
            }

            Result.success(domainItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchGamesForSubcategory(subcategoryId: String): Result<List<GameItem>> {
        return try {
            val assignmentsUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/subcategory_games?subcategory_id=eq.$subcategoryId&is_active=eq.true&order=sort_order.asc"
            val assignmentsResponse: HttpResponse = client.get(assignmentsUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (assignmentsResponse.status.value !in 200..299) {
                return Result.failure(Exception("Failed to fetch game assignments: HTTP ${assignmentsResponse.status.value}"))
            }

            val assignmentEntities: List<SubcategoryGameEntity> = assignmentsResponse.body()
            if (assignmentEntities.isEmpty()) {
                return Result.success(emptyList())
            }

            val gameIds = assignmentEntities.map { it.gameId }.distinct()
            val idsParam = gameIds.joinToString(",")
            val gamesUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/games?id=in.($idsParam)&is_published=eq.true"

            val gamesResponse: HttpResponse = client.get(gamesUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (gamesResponse.status.value !in 200..299) {
                return Result.failure(Exception("Failed to fetch game definitions: HTTP ${gamesResponse.status.value}"))
            }

            val gameEntities: List<GameEntity> = gamesResponse.body()
            val gameMap = gameEntities.associateBy { it.id }

            val domainGames = assignmentEntities.mapNotNull { assignment ->
                val game = gameMap[assignment.gameId]
                game?.toDomain(assignment)
            }

            Result.success(domainGames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchQuestions(subcategoryId: String, gameId: String): Result<List<Question>> {
        return try {
            val questionsUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/questions?game_id=eq.$gameId&subcategory_id=eq.$subcategoryId&is_draft=eq.false&order=sort_order.asc"
            val questionsResponse: HttpResponse = client.get(questionsUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (questionsResponse.status.value !in 200..299) {
                return Result.failure(Exception("Failed to fetch questions: HTTP ${questionsResponse.status.value}"))
            }

            val questionEntities: List<QuestionEntity> = questionsResponse.body()
            if (questionEntities.isEmpty()) {
                return Result.success(emptyList())
            }

            val questionIds = questionEntities.map { it.id }.distinct()
            val optionsMap = mutableMapOf<String, List<QuestionOptionEntity>>()
            if (questionIds.isNotEmpty()) {
                val idsParam = questionIds.joinToString(",")
                val optionsUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/question_options?question_id=in.($idsParam)&order=sort_order.asc"
                try {
                    val optionsResponse: HttpResponse = client.get(optionsUrl) {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                        header(HttpHeaders.Accept, "application/json")
                    }
                    if (optionsResponse.status.value in 200..299) {
                        val optionEntities: List<QuestionOptionEntity> = optionsResponse.body()
                        val grouped = optionEntities.groupBy { it.questionId }
                        optionsMap.putAll(grouped)
                    }
                } catch (_: Exception) {
                    // Ignore options query RLS or network error
                }
            }

            val domainQuestions = questionEntities.map { qEntity ->
                val opts = optionsMap[qEntity.id]?.map { optEntity -> optEntity.toDomain() } ?: emptyList()
                qEntity.toDomain(options = opts)
            }

            Result.success(domainQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
