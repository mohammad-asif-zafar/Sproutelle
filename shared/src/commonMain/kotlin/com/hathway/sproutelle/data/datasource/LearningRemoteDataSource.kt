package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.data.model.GameConfigEntity
import com.hathway.sproutelle.data.model.GameEntity
import com.hathway.sproutelle.data.model.GameLevelEntity
import com.hathway.sproutelle.data.model.MediaAssetEntity
import com.hathway.sproutelle.data.model.QuestionEntity
import com.hathway.sproutelle.data.model.QuestionOptionEntity
import com.hathway.sproutelle.data.model.SubcategoryEntity
import com.hathway.sproutelle.data.model.SubcategoryGameEntity
import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
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
    suspend fun fetchGamesForExplorer(explorerId: String): Result<List<SubcategoryWithGames>>
    suspend fun fetchQuestions(subcategoryId: String, gameId: String): Result<List<Question>>
    suspend fun fetchGameConfig(gameId: String): Result<GameConfig>
    suspend fun fetchGameLevels(gameId: String): Result<List<GameLevel>>
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

    override suspend fun fetchGameConfig(gameId: String): Result<GameConfig> {
        return try {
            val configUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/games?id=eq.$gameId&is_published=eq.true"
            val response: HttpResponse = client.get(configUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (response.status.value !in 200..299) {
                return Result.success(GameConfig(gameId = gameId, name = "Picture Quest"))
            }

            val gameConfigs: List<GameConfigEntity> = response.body()
            val configEntity = gameConfigs.firstOrNull()
                ?: return Result.success(GameConfig(gameId = gameId, name = "Picture Quest"))

            Result.success(configEntity.toDomain())
        } catch (_: Exception) {
            Result.success(GameConfig(gameId = gameId, name = "Picture Quest"))
        }
    }

    override suspend fun fetchGameLevels(gameId: String): Result<List<GameLevel>> {
        return try {
            val levelsUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/game_levels?game_id=eq.$gameId&is_published=eq.true&order=level_number.asc"
            val response: HttpResponse = client.get(levelsUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (response.status.value !in 200..299) {
                return Result.success(emptyList())
            }

            val levelEntities: List<GameLevelEntity> = response.body()
            Result.success(levelEntities.map { it.toDomain() })
        } catch (_: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun fetchSubcategories(explorerId: String): Result<List<Subcategory>> {
        return try {
            val subcategoriesUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/homescreen_explorer_subcategories?explorer_id=eq.$explorerId&is_active=eq.true&order=sort_order.asc"
            println("SupabaseLearningRemoteDataSource: GET $subcategoriesUrl")
            val response: HttpResponse = client.get(subcategoriesUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            val statusCode = response.status.value
            val responseText = response.bodyAsText()
            println("SupabaseLearningRemoteDataSource: HTTP $statusCode - $responseText")

            if (statusCode !in 200..299) {
                return Result.failure(Exception("Failed to fetch subcategories: HTTP $statusCode ($responseText)"))
            }

            val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
            val subcategoryEntities: List<SubcategoryEntity> = jsonParser.decodeFromString(responseText)

            val assetIds = subcategoryEntities.mapNotNull { it.imageAssetId }.distinct()
            val mediaMap = fetchMediaAssets(assetIds)
            val signedUrlMap = createSignedUrls(mediaMap.values.toList())

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

    override suspend fun fetchGamesForExplorer(explorerId: String): Result<List<SubcategoryWithGames>> {
        return try {
            val subcategoriesResult = fetchSubcategories(explorerId)
            if (subcategoriesResult.isFailure) {
                return Result.failure(subcategoriesResult.exceptionOrNull() ?: Exception("Failed to fetch subcategories for explorer"))
            }
            val subcategories = subcategoriesResult.getOrDefault(emptyList()).filter { it.isActive }
            if (subcategories.isEmpty()) {
                return Result.success(emptyList())
            }

            val subcategoryIds = subcategories.map { it.id }
            val idsParam = subcategoryIds.joinToString(",")
            val assignmentsUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/subcategory_games?subcategory_id=in.($idsParam)&is_active=eq.true&order=sort_order.asc"
            val assignmentsResponse: HttpResponse = client.get(assignmentsUrl) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                header(HttpHeaders.Accept, "application/json")
            }

            if (assignmentsResponse.status.value !in 200..299) {
                return Result.failure(Exception("Failed to fetch game assignments for explorer: HTTP ${assignmentsResponse.status.value}"))
            }

            val assignmentEntities: List<SubcategoryGameEntity> = assignmentsResponse.body()
            val gameIds = assignmentEntities.map { it.gameId }.distinct()

            val gameMap = if (gameIds.isNotEmpty()) {
                val gameIdsParam = gameIds.joinToString(",")
                val gamesUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/games?id=in.($gameIdsParam)&is_published=eq.true"
                val gamesResponse: HttpResponse = client.get(gamesUrl) {
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    header(HttpHeaders.Accept, "application/json")
                }
                if (gamesResponse.status.value in 200..299) {
                    val gameEntities: List<GameEntity> = gamesResponse.body()
                    gameEntities.associateBy { it.id }
                } else {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val subcategoryWithGamesList = subcategories.map { subcategory ->
                val assignments = assignmentEntities.filter { it.subcategoryId == subcategory.id }
                val games = assignments.mapNotNull { assignment ->
                    val game = gameMap[assignment.gameId]
                    game?.toDomain(assignment)
                }.filter { it.isActive && it.isPublished }
                SubcategoryWithGames(subcategory, games)
            }

            Result.success(subcategoryWithGamesList)
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
                    // Ignore RLS or 401/403 errors on question_options
                }
            }

            val allAssetIds = mutableListOf<String>()
            questionEntities.forEach { q ->
                q.imageAssetId?.let { allAssetIds.add(it) }
                q.questionIllustrationAssetId?.let { allAssetIds.add(it) }
            }
            optionsMap.values.flatten().forEach { opt ->
                opt.imageAssetId?.let { allAssetIds.add(it) }
            }

            val mediaMap = fetchMediaAssets(allAssetIds.distinct())
            val signedUrlMap = createSignedUrls(mediaMap.values.toList())

            val domainQuestions = questionEntities.map { qEntity ->
                val opts = optionsMap[qEntity.id]?.map { optEntity ->
                    val optAsset = optEntity.imageAssetId?.let { mediaMap[it] }
                    val optImageUrl = optAsset?.filePath?.let { signedUrlMap[it] }
                    optEntity.toDomain(imageUrl = optImageUrl)
                } ?: emptyList()

                val qAsset = qEntity.imageAssetId?.let { mediaMap[it] }
                val qImageUrl = qAsset?.filePath?.let { signedUrlMap[it] }

                val qIllusAsset = qEntity.questionIllustrationAssetId?.let { mediaMap[it] }
                val qIllusUrl = qIllusAsset?.filePath?.let { signedUrlMap[it] }

                qEntity.toDomain(
                    imageUrl = qImageUrl,
                    questionIllustrationUrl = qIllusUrl,
                    options = opts
                )
            }

            Result.success(domainQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchMediaAssets(assetIds: List<String>): Map<String, MediaAssetEntity> {
        if (assetIds.isEmpty()) return emptyMap()
        val idsParam = assetIds.joinToString(",")
        val mediaUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/media_assets?id=in.($idsParam)&select=id,name,bucket_name,file_path,mime_type,size_bytes,checksum_sha256,created_by,created_at"
        return try {
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
    }

    private suspend fun createSignedUrls(assets: List<MediaAssetEntity>): Map<String, String> {
        if (assets.isEmpty()) return emptyMap()
        val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
        val pathsByBucket = mutableMapOf<String, MutableList<String>>()
        assets.forEach { asset ->
            val bucketName = asset.bucketName?.ifBlank { "game-assets" } ?: "game-assets"
            val filePath = asset.filePath
            if (!filePath.isNullOrBlank()) {
                pathsByBucket.getOrPut(bucketName) { mutableListOf() }.add(filePath)
            }
        }

        val signedUrlMap = mutableMapOf<String, String>()
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
        return signedUrlMap
    }
}
