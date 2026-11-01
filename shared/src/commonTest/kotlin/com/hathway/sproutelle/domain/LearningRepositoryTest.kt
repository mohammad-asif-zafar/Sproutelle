package com.hathway.sproutelle.domain

import com.hathway.sproutelle.data.datasource.LearningCacheStorage
import com.hathway.sproutelle.data.datasource.LearningRemoteDataSource
import com.hathway.sproutelle.data.model.GameEntity
import com.hathway.sproutelle.data.model.SubcategoryGameEntity
import com.hathway.sproutelle.data.repository.LearningRepositoryImpl
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LearningRepositoryTest {

    private class FakeLearningRemoteDataSource(
        var subcategoriesToReturn: Map<String, Result<List<Subcategory>>> = emptyMap(),
        var gamesToReturn: Map<String, Result<List<GameItem>>> = emptyMap(),
        var questionsToReturn: Map<String, Result<List<Question>>> = emptyMap()
    ) : LearningRemoteDataSource {
        var subcategoryFetchCount = 0
        var gameFetchCount = 0
        var questionFetchCount = 0

        override suspend fun fetchSubcategories(explorerId: String): Result<List<Subcategory>> {
            subcategoryFetchCount++
            return subcategoriesToReturn[explorerId] ?: Result.success(emptyList())
        }

        override suspend fun fetchGamesForSubcategory(subcategoryId: String): Result<List<GameItem>> {
            gameFetchCount++
            return gamesToReturn[subcategoryId] ?: Result.success(emptyList())
        }

        override suspend fun fetchQuestions(subcategoryId: String, gameId: String): Result<List<Question>> {
            questionFetchCount++
            val key = "${subcategoryId}_${gameId}"
            return questionsToReturn[key] ?: Result.success(emptyList())
        }
    }

    private class FakeLearningCacheStorage(
        initialSubcategories: Map<String, List<Subcategory>> = emptyMap(),
        initialGames: Map<String, List<GameItem>> = emptyMap(),
        initialQuestions: Map<String, List<Question>> = emptyMap()
    ) : LearningCacheStorage {
        val storedSubcategories = initialSubcategories.toMutableMap()
        val storedGames = initialGames.toMutableMap()
        val storedQuestions = initialQuestions.toMutableMap()

        override suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>) {
            storedSubcategories[explorerId] = items
        }

        override suspend fun loadSubcategories(explorerId: String): List<Subcategory> {
            return storedSubcategories[explorerId] ?: emptyList()
        }

        override fun loadSubcategoriesSync(explorerId: String): List<Subcategory> {
            return storedSubcategories[explorerId] ?: emptyList()
        }

        override suspend fun saveGames(subcategoryId: String, items: List<GameItem>) {
            storedGames[subcategoryId] = items
        }

        override suspend fun loadGames(subcategoryId: String): List<GameItem> {
            return storedGames[subcategoryId] ?: emptyList()
        }

        override fun loadGamesSync(subcategoryId: String): List<GameItem> {
            return storedGames[subcategoryId] ?: emptyList()
        }

        override suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>) {
            storedQuestions["${subcategoryId}_${gameId}"] = items
        }

        override suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question> {
            return storedQuestions["${subcategoryId}_${gameId}"] ?: emptyList()
        }

        override fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question> {
            return storedQuestions["${subcategoryId}_${gameId}"] ?: emptyList()
        }
    }

    @Test
    fun test1_SubcategoriesLoadingByExplorerId(): Unit = runBlocking {
        val explorerId = "exp_1"
        val subcategories = listOf(
            Subcategory("sub_1", explorerId, "Fruits", "asset1", "#E8F8EE", null, 1, true, "url1")
        )
        val remote = FakeLearningRemoteDataSource(subcategoriesToReturn = mapOf(explorerId to Result.success(subcategories)))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val state = repository.getSubcategoriesState(explorerId).first { it is LearningState.Fresh }
        assertNotNull(state)
        val loaded = (state as LearningState.Fresh).data
        assertEquals(1, loaded.size)
        assertEquals("Fruits", loaded[0].name)
        assertEquals(explorerId, loaded[0].explorerId)
    }

    @Test
    fun test2_GameAssignmentsLoadingBySubcategoryId(): Unit = runBlocking {
        val subcategoryId = "sub_1"
        val games = listOf(
            GameItem("assign_1", subcategoryId, "game_1", "Guess Game", "guess-game", "Desc", "image_selection", true, 1, true)
        )
        val remote = FakeLearningRemoteDataSource(gamesToReturn = mapOf(subcategoryId to Result.success(games)))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val state = repository.getGamesForSubcategoryState(subcategoryId).first { it is LearningState.Fresh }
        assertNotNull(state)
        val loaded = (state as LearningState.Fresh).data
        assertEquals(1, loaded.size)
        assertEquals("Guess Game", loaded[0].name)
        assertEquals(subcategoryId, loaded[0].subcategoryId)
        assertEquals("game_1", loaded[0].gameId)
    }

    @Test
    fun test3_SameGameAssignedToMultipleSubcategories(): Unit = runBlocking {
        val gameId = "shared_game_1"
        val sub1 = "sub_1"
        val sub2 = "sub_2"

        val gamesSub1 = listOf(GameItem("assign_1", sub1, gameId, "Shared Quiz", "quiz", "Desc", "quiz", true, 1, true))
        val gamesSub2 = listOf(GameItem("assign_2", sub2, gameId, "Shared Quiz", "quiz", "Desc", "quiz", true, 1, true))

        val remote = FakeLearningRemoteDataSource(gamesToReturn = mapOf(
            sub1 to Result.success(gamesSub1),
            sub2 to Result.success(gamesSub2)
        ))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val loadedSub1 = repository.getGamesForSubcategory(sub1).first { it.isNotEmpty() }
        val loadedSub2 = repository.getGamesForSubcategory(sub2).first { it.isNotEmpty() }

        assertEquals(1, loadedSub1.size)
        assertEquals(1, loadedSub2.size)
        assertEquals(sub1, loadedSub1[0].subcategoryId)
        assertEquals(sub2, loadedSub2[0].subcategoryId)
        assertEquals(gameId, loadedSub1[0].gameId)
        assertEquals(gameId, loadedSub2[0].gameId)
    }

    @Test
    fun test4_InactiveAndUnpublishedAssignmentsExcluded(): Unit = runBlocking {
        val subcategoryId = "sub_1"
        val games = listOf(
            GameItem("assign_1", subcategoryId, "game_active", "Active Game", "active", "Desc", "quiz", true, 1, true),
            GameItem("assign_2", subcategoryId, "game_inactive", "Inactive Game", "inactive", "Desc", "quiz", true, 2, false),
            GameItem("assign_3", subcategoryId, "game_unpublished", "Unpublished", "unpub", "Desc", "quiz", false, 3, true)
        )
        val remote = FakeLearningRemoteDataSource(gamesToReturn = mapOf(subcategoryId to Result.success(games)))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val loaded = repository.getGamesForSubcategory(subcategoryId).first { it.isNotEmpty() }
        assertEquals(1, loaded.size)
        assertEquals("Active Game", loaded[0].name)
    }

    @Test
    fun test5_CacheFirstBehaviorAndBackgroundRefresh(): Unit = runBlocking {
        val explorerId = "exp_1"
        val cached = listOf(Subcategory("sub_cached", explorerId, "Cached Topic", null, "#fff", null, 1, true))
        val fresh = listOf(Subcategory("sub_fresh", explorerId, "Fresh Topic", null, "#fff", null, 1, true))

        val cache = FakeLearningCacheStorage(initialSubcategories = mapOf(explorerId to cached))
        val remote = FakeLearningRemoteDataSource(subcategoriesToReturn = mapOf(explorerId to Result.success(fresh)))
        val repository = LearningRepositoryImpl(remote, cache)

        val cachedState = repository.getSubcategoriesState(explorerId).first { it is LearningState.Cached }
        assertEquals(cached, (cachedState as LearningState.Cached).data)

        val freshState = repository.getSubcategoriesState(explorerId).first { it is LearningState.Fresh }
        assertEquals(fresh, (freshState as LearningState.Fresh).data)
    }

    @Test
    fun test6_OfflineFallbackPreservesCache(): Unit = runBlocking {
        val explorerId = "exp_1"
        val cached = listOf(Subcategory("sub_cached", explorerId, "Cached Topic", null, "#fff", null, 1, true))

        val cache = FakeLearningCacheStorage(initialSubcategories = mapOf(explorerId to cached))
        val remote = FakeLearningRemoteDataSource(subcategoriesToReturn = mapOf(explorerId to Result.failure(Exception("Offline"))))
        val repository = LearningRepositoryImpl(remote, cache)

        val state = repository.getSubcategoriesState(explorerId).first { it is LearningState.Offline }
        assertEquals(cached, (state as LearningState.Offline).data)
    }

    @Test
    fun test7_NavigationIdPreservation(): Unit = runBlocking {
        val subcategoryId = "sub_xyz"
        val gameId = "game_abc"
        val games = listOf(
            GameItem("assign_99", subcategoryId, gameId, "Memory Game", "memory", "Desc", "matching", true, 1, true)
        )
        val remote = FakeLearningRemoteDataSource(gamesToReturn = mapOf(subcategoryId to Result.success(games)))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val items = repository.getGamesForSubcategory(subcategoryId).first { it.isNotEmpty() }
        assertEquals(1, items.size)
        assertEquals(subcategoryId, items[0].subcategoryId)
        assertEquals(gameId, items[0].gameId)
    }

    @Test
    fun test8_QuestionsLoadingBySubcategoryIdAndGameId(): Unit = runBlocking {
        val subcategoryId = "sub_fruits"
        val gameId = "game_quiz"
        val questions = listOf(
            Question(
                id = "q1",
                gameId = gameId,
                subcategoryId = subcategoryId,
                text = "Find the Watermelon?",
                type = "image_selection",
                instructions = null,
                sortOrder = 1,
                isDraft = false,
                options = listOf(
                    QuestionOption("opt1", "q1", "Watermelon 🍉", true, 1),
                    QuestionOption("opt2", "q1", "Apple 🍎", false, 2)
                )
            )
        )

        val remote = FakeLearningRemoteDataSource(questionsToReturn = mapOf("${subcategoryId}_${gameId}" to Result.success(questions)))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val state = repository.getQuestionsState(subcategoryId, gameId).first { it is LearningState.Fresh }
        assertNotNull(state)
        val loaded = (state as LearningState.Fresh).data
        assertEquals(1, loaded.size)
        assertEquals("Find the Watermelon?", loaded[0].text)
        assertEquals(2, loaded[0].options.size)
        assertEquals(true, loaded[0].options[0].isCorrect)
    }

    @Test
    fun test9_IndependentQuestionsSameGameTwoSubcategories(): Unit = runBlocking {
        val gameId = "shared_quiz"
        val sub1 = "sub_fruits"
        val sub2 = "sub_animals"

        val qSub1 = listOf(Question("q_fruit", gameId, sub1, "Find Apple?", "quiz", null, 1, false))
        val qSub2 = listOf(Question("q_animal", gameId, sub2, "Find Lion?", "quiz", null, 1, false))

        val remote = FakeLearningRemoteDataSource(questionsToReturn = mapOf(
            "${sub1}_${gameId}" to Result.success(qSub1),
            "${sub2}_${gameId}" to Result.success(qSub2)
        ))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val loaded1 = repository.getQuestions(sub1, gameId).first { it.isNotEmpty() }
        val loaded2 = repository.getQuestions(sub2, gameId).first { it.isNotEmpty() }

        assertEquals("Find Apple?", loaded1[0].text)
        assertEquals("Find Lion?", loaded2[0].text)
    }

    @Test
    fun test10_DraftQuestionsExcluded(): Unit = runBlocking {
        val sub1 = "sub_fruits"
        val gameId = "game_quiz"

        val questions = listOf(
            Question("q_active", gameId, sub1, "Active Q", "quiz", null, 1, isDraft = false),
            Question("q_draft", gameId, sub1, "Draft Q", "quiz", null, 2, isDraft = true)
        )

        val remote = FakeLearningRemoteDataSource(questionsToReturn = mapOf("${sub1}_${gameId}" to Result.success(questions)))
        val cache = FakeLearningCacheStorage()
        val repository = LearningRepositoryImpl(remote, cache)

        val loaded = repository.getQuestions(sub1, gameId).first { it.isNotEmpty() }
        assertEquals(1, loaded.size)
        assertEquals("Active Q", loaded[0].text)
    }

    @Test
    fun test11_QuestionsCacheFirstAndBackgroundRefresh(): Unit = runBlocking {
        val sub1 = "sub_fruits"
        val gameId = "game_quiz"

        val cached = listOf(Question("q_cached", gameId, sub1, "Cached Question?", "quiz", null, 1, false))
        val fresh = listOf(Question("q_fresh", gameId, sub1, "Fresh Question?", "quiz", null, 1, false))

        val cache = FakeLearningCacheStorage(initialQuestions = mapOf("${sub1}_${gameId}" to cached))
        val remote = FakeLearningRemoteDataSource(questionsToReturn = mapOf("${sub1}_${gameId}" to Result.success(fresh)))
        val repository = LearningRepositoryImpl(remote, cache)

        val cachedState = repository.getQuestionsState(sub1, gameId).first { it is LearningState.Cached }
        assertEquals(cached, (cachedState as LearningState.Cached).data)

        val freshState = repository.getQuestionsState(sub1, gameId).first { it is LearningState.Fresh }
        assertEquals(fresh, (freshState as LearningState.Fresh).data)
    }

    @Test
    fun test12_QuestionsOfflineFallbackPreservesCache(): Unit = runBlocking {
        val sub1 = "sub_fruits"
        val gameId = "game_quiz"

        val cached = listOf(Question("q_cached", gameId, sub1, "Cached Question?", "quiz", null, 1, false))

        val cache = FakeLearningCacheStorage(initialQuestions = mapOf("${sub1}_${gameId}" to cached))
        val remote = FakeLearningRemoteDataSource(questionsToReturn = mapOf("${sub1}_${gameId}" to Result.failure(Exception("Network offline"))))
        val repository = LearningRepositoryImpl(remote, cache)

        val state = repository.getQuestionsState(sub1, gameId).first { it is LearningState.Offline }
        assertEquals(cached, (state as LearningState.Offline).data)
    }

    @Test
    fun test13_NavigationContextPreservationState() {
        val explorerId = "exp_everyday"
        val explorerTitle = "Everyday World"
        val subcategoryId = "sub_fruits"
        val subcategoryName = "Guess Fruits"
        val gameId = "game_quiz"
        val gameName = "Quizzy Pop"

        val gameSelection = com.hathway.sproutelle.core.navigation.Screen.GameSelection(
            explorerId = explorerId,
            explorerTitle = explorerTitle,
            subcategoryId = subcategoryId,
            subcategoryName = subcategoryName
        )
        val questionScreen = com.hathway.sproutelle.core.navigation.Screen.Question(
            explorerId = explorerId,
            explorerTitle = explorerTitle,
            subcategoryId = subcategoryId,
            gameId = gameId,
            gameName = gameName,
            subcategoryName = subcategoryName
        )

        assertEquals(explorerId, gameSelection.explorerId)
        assertEquals(explorerTitle, gameSelection.explorerTitle)
        assertEquals(subcategoryId, gameSelection.subcategoryId)
        assertEquals(subcategoryName, gameSelection.subcategoryName)

        assertEquals(explorerId, questionScreen.explorerId)
        assertEquals(explorerTitle, questionScreen.explorerTitle)
        assertEquals(subcategoryId, questionScreen.subcategoryId)
        assertEquals(gameId, questionScreen.gameId)
        assertEquals(subcategoryName, questionScreen.subcategoryName)
    }

    @Test
    fun test14_MalformedGameConfigJsonParsing() {
        val assignment = SubcategoryGameEntity("a1", "sub1", "g1")

        val malformedGame = GameEntity(
            id = "g1",
            name = "Test Game",
            config = JsonPrimitive("invalid_json_type_instead_of_object")
        )

        val domainItem = malformedGame.toDomain(assignment)
        assertEquals("Test Game", domainItem.name)
        assertEquals(null, domainItem.minAge)
        assertEquals(null, domainItem.maxAge)
        assertEquals(null, domainItem.difficulty)
        assertEquals(null, domainItem.questionCount)
    }
}
