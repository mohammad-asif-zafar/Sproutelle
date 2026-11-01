package com.hathway.sproutelle.data.model

import com.hathway.sproutelle.domain.model.GameItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class SubcategoryGameEntity(
    @SerialName("id") val id: String,
    @SerialName("subcategory_id") val subcategoryId: String,
    @SerialName("game_id") val gameId: String,
    @SerialName("enabled_patterns") val enabledPatterns: JsonElement? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class GameEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("game_type") val gameType: String = "general",
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("config") val config: JsonElement? = null
) {
    fun toDomain(assignment: SubcategoryGameEntity): GameItem {
        val configObj = try { config as? JsonObject } catch (_: Exception) { null }
        val minAge = try { configObj?.get("min_age")?.jsonPrimitive?.intOrNull } catch (_: Exception) { null }
        val maxAge = try { configObj?.get("max_age")?.jsonPrimitive?.intOrNull } catch (_: Exception) { null }
        val difficulty = try { configObj?.get("difficulty")?.jsonPrimitive?.content } catch (_: Exception) { null }
        val questionCount = try { configObj?.get("question_count")?.jsonPrimitive?.intOrNull } catch (_: Exception) { null }

        return GameItem(
            assignmentId = assignment.id,
            subcategoryId = assignment.subcategoryId,
            gameId = id,
            name = name,
            slug = slug,
            description = description,
            gameType = gameType,
            isPublished = isPublished,
            sortOrder = assignment.sortOrder,
            isActive = assignment.isActive,
            minAge = minAge,
            maxAge = maxAge,
            difficulty = difficulty,
            questionCount = questionCount
        )
    }
}
