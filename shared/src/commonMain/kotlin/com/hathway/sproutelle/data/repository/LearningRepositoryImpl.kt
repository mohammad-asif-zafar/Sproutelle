package com.hathway.sproutelle.data.repository

import com.hathway.sproutelle.core.currentTimeMillis
import com.hathway.sproutelle.data.datasource.LearningCache
import com.hathway.sproutelle.data.datasource.LearningCacheStorage
import com.hathway.sproutelle.data.datasource.LearningRemoteDataSource
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LearningRepositoryImpl(
    private val remoteDataSource: LearningRemoteDataSource,
    private val cacheStorage: LearningCacheStorage = LearningCache(),
    private val clock: () -> Long = { currentTimeMillis() }
) : LearningRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val subcategoryStates = mutableMapOf<String, MutableStateFlow<LearningState<List<Subcategory>>>>()
    private val subcategoryInitMutex = Mutex()
    private val subcategoryRefreshMutex = Mutex()
    private val subcategoryInFlight = mutableMapOf<String, Deferred<Result<Unit>>>()
    private val initializedSubcategories = mutableSetOf<String>()

    private val gameStates = mutableMapOf<String, MutableStateFlow<LearningState<List<GameItem>>>>()
    private val gameInitMutex = Mutex()
    private val gameRefreshMutex = Mutex()
    private val gameInFlight = mutableMapOf<String, Deferred<Result<Unit>>>()
    private val initializedGames = mutableSetOf<String>()

    private val explorerGamesStates = mutableMapOf<String, MutableStateFlow<LearningState<List<SubcategoryWithGames>>>>()
    private val explorerGamesInitMutex = Mutex()
    private val explorerGamesRefreshMutex = Mutex()
    private val explorerGamesInFlight = mutableMapOf<String, Deferred<Result<Unit>>>()
    private val initializedExplorerGames = mutableSetOf<String>()

    private val questionStates = mutableMapOf<String, MutableStateFlow<LearningState<List<Question>>>>()
    private val questionInitMutex = Mutex()
    private val questionRefreshMutex = Mutex()
    private val questionInFlight = mutableMapOf<String, Deferred<Result<Unit>>>()
    private val initializedQuestions = mutableSetOf<String>()

    private fun getOrCreateSubcategoryState(explorerId: String): MutableStateFlow<LearningState<List<Subcategory>>> {
        return subcategoryStates.getOrPut(explorerId) {
            MutableStateFlow(LearningState.Loading)
        }
    }

    private fun getOrCreateGameState(subcategoryId: String): MutableStateFlow<LearningState<List<GameItem>>> {
        return gameStates.getOrPut(subcategoryId) {
            MutableStateFlow(LearningState.Loading)
        }
    }

    private fun getOrCreateExplorerGameState(explorerId: String): MutableStateFlow<LearningState<List<SubcategoryWithGames>>> {
        return explorerGamesStates.getOrPut(explorerId) {
            MutableStateFlow(LearningState.Loading)
        }
    }

    private fun getOrCreateQuestionState(key: String): MutableStateFlow<LearningState<List<Question>>> {
        return questionStates.getOrPut(key) {
            MutableStateFlow(LearningState.Loading)
        }
    }

    private suspend fun ensureSubcategoriesInitialized(explorerId: String) {
        if (initializedSubcategories.contains(explorerId)) return
        subcategoryInitMutex.withLock {
            if (!initializedSubcategories.contains(explorerId)) {
                val flow = getOrCreateSubcategoryState(explorerId)
                try {
                    val cached = cacheStorage.loadSubcategories(explorerId)
                    if (cached.isNotEmpty()) {
                        flow.value = LearningState.Cached(cached)
                    }
                } catch (e: Exception) {
                    println("LearningRepository: Subcategories cache load error: ${e.message}")
                }
                initializedSubcategories.add(explorerId)
                refreshSubcategories(explorerId)
            }
        }
    }

    private suspend fun ensureGamesInitialized(subcategoryId: String) {
        if (initializedGames.contains(subcategoryId)) return
        gameInitMutex.withLock {
            if (!initializedGames.contains(subcategoryId)) {
                val flow = getOrCreateGameState(subcategoryId)
                try {
                    val cached = cacheStorage.loadGames(subcategoryId)
                    if (cached.isNotEmpty()) {
                        flow.value = LearningState.Cached(cached)
                    }
                } catch (e: Exception) {
                    println("LearningRepository: Games cache load error: ${e.message}")
                }
                initializedGames.add(subcategoryId)
                refreshGamesForSubcategory(subcategoryId)
            }
        }
    }

    private suspend fun ensureExplorerGamesInitialized(explorerId: String) {
        if (initializedExplorerGames.contains(explorerId)) return
        explorerGamesInitMutex.withLock {
            if (!initializedExplorerGames.contains(explorerId)) {
                val flow = getOrCreateExplorerGameState(explorerId)
                try {
                    val cached = cacheStorage.loadGamesForExplorer(explorerId)
                    if (cached.isNotEmpty()) {
                        flow.value = LearningState.Cached(cached)
                    }
                } catch (e: Exception) {
                    println("LearningRepository: Explorer games cache load error: ${e.message}")
                }
                initializedExplorerGames.add(explorerId)
                refreshGamesForExplorer(explorerId)
            }
        }
    }

    private suspend fun ensureQuestionsInitialized(subcategoryId: String, gameId: String) {
        val key = "${subcategoryId}_${gameId}"
        if (initializedQuestions.contains(key)) return
        questionInitMutex.withLock {
            if (!initializedQuestions.contains(key)) {
                val flow = getOrCreateQuestionState(key)
                try {
                    val cached = cacheStorage.loadQuestions(subcategoryId, gameId)
                    if (cached.isNotEmpty()) {
                        flow.value = LearningState.Cached(cached)
                    }
                } catch (e: Exception) {
                    println("LearningRepository: Questions cache load error: ${e.message}")
                }
                initializedQuestions.add(key)
                refreshQuestions(subcategoryId, gameId)
            }
        }
    }

    override fun getSubcategories(explorerId: String): Flow<List<Subcategory>> {
        return getSubcategoriesState(explorerId).map { state ->
            when (state) {
                is LearningState.Cached -> state.data
                is LearningState.Fresh -> state.data
                is LearningState.Offline -> state.data
                is LearningState.Error -> state.cachedData ?: emptyList()
                is LearningState.Loading -> emptyList()
            }
        }
    }

    override fun getSubcategoriesState(explorerId: String): Flow<LearningState<List<Subcategory>>> {
        val flow = getOrCreateSubcategoryState(explorerId)
        repositoryScope.launch {
            ensureSubcategoriesInitialized(explorerId)
        }
        return flow.asStateFlow()
    }

    override suspend fun refreshSubcategories(explorerId: String): Result<Unit> {
        val flow = getOrCreateSubcategoryState(explorerId)
        val jobToAwait = subcategoryRefreshMutex.withLock {
            subcategoryInFlight[explorerId]?.let { return@withLock it }
            val deferred = repositoryScope.async {
                try {
                    val result = remoteDataSource.fetchSubcategories(explorerId)
                    result.onSuccess { fetched ->
                        val activeItems = fetched.filter { it.isActive }
                        val currentState = flow.value
                        val currentItems = when (currentState) {
                            is LearningState.Cached -> currentState.data
                            is LearningState.Fresh -> currentState.data
                            is LearningState.Offline -> currentState.data
                            is LearningState.Error -> currentState.cachedData ?: emptyList()
                            is LearningState.Loading -> emptyList()
                        }

                        val currentMap = currentItems.associateBy { it.id }
                        val mergedItems = activeItems.map { newItem ->
                            val oldItem = currentMap[newItem.id]
                            val now = clock()
                            val expiryWindow = 24 * 3600 * 1000L
                            val safetyBuffer = 4 * 3600 * 1000L
                            val isUrlValid = !oldItem?.imageUrl.isNullOrBlank() &&
                                    (now - oldItem.signedAtTimestamp < expiryWindow - safetyBuffer) &&
                                    oldItem.imageAssetId == newItem.imageAssetId

                            if (newItem.imageUrl.isNullOrBlank() && isUrlValid) {
                                newItem.copy(imageUrl = oldItem.imageUrl, signedAtTimestamp = oldItem.signedAtTimestamp)
                            } else {
                                newItem.copy(signedAtTimestamp = if (newItem.imageUrl != null) now else 0L)
                            }
                        }

                        flow.value = LearningState.Fresh(mergedItems)
                        try {
                            cacheStorage.saveSubcategories(explorerId, mergedItems)
                        } catch (e: Exception) {
                            println("LearningRepository: Failed to persist subcategories cache: ${e.message}")
                        }
                    }.onFailure { exception ->
                        val currentState = flow.value
                        val cachedData = when (currentState) {
                            is LearningState.Cached -> currentState.data
                            is LearningState.Fresh -> currentState.data
                            is LearningState.Offline -> currentState.data
                            is LearningState.Error -> currentState.cachedData
                            is LearningState.Loading -> {
                                try { cacheStorage.loadSubcategories(explorerId) } catch (_: Exception) { emptyList() }
                            }
                        }
                        if (!cachedData.isNullOrEmpty()) {
                            flow.value = LearningState.Offline(cachedData, exception.message)
                        } else {
                            flow.value = LearningState.Error(exception.message ?: "Failed to load subcategories", null)
                        }
                    }
                    result.map { }
                } finally {
                    subcategoryRefreshMutex.withLock {
                        subcategoryInFlight.remove(explorerId)
                    }
                }
            }
            subcategoryInFlight[explorerId] = deferred
            deferred
        }
        return jobToAwait.await()
    }

    override fun getGamesForSubcategory(subcategoryId: String): Flow<List<GameItem>> {
        return getGamesForSubcategoryState(subcategoryId).map { state ->
            when (state) {
                is LearningState.Cached -> state.data
                is LearningState.Fresh -> state.data
                is LearningState.Offline -> state.data
                is LearningState.Error -> state.cachedData ?: emptyList()
                is LearningState.Loading -> emptyList()
            }
        }
    }

    override fun getGamesForSubcategoryState(subcategoryId: String): Flow<LearningState<List<GameItem>>> {
        val flow = getOrCreateGameState(subcategoryId)
        repositoryScope.launch {
            ensureGamesInitialized(subcategoryId)
        }
        return flow.asStateFlow()
    }

    override suspend fun refreshGamesForSubcategory(subcategoryId: String): Result<Unit> {
        val flow = getOrCreateGameState(subcategoryId)
        val jobToAwait = gameRefreshMutex.withLock {
            gameInFlight[subcategoryId]?.let { return@withLock it }
            val deferred = repositoryScope.async {
                try {
                    val result = remoteDataSource.fetchGamesForSubcategory(subcategoryId)
                    result.onSuccess { fetched ->
                        val activeGames = fetched.filter { it.isActive && it.isPublished }
                        flow.value = LearningState.Fresh(activeGames)
                        try {
                            cacheStorage.saveGames(subcategoryId, activeGames)
                        } catch (e: Exception) {
                            println("LearningRepository: Failed to persist games cache: ${e.message}")
                        }
                    }.onFailure { exception ->
                        val currentState = flow.value
                        val cachedData = when (currentState) {
                            is LearningState.Cached -> currentState.data
                            is LearningState.Fresh -> currentState.data
                            is LearningState.Offline -> currentState.data
                            is LearningState.Error -> currentState.cachedData
                            is LearningState.Loading -> {
                                try { cacheStorage.loadGames(subcategoryId) } catch (_: Exception) { emptyList() }
                            }
                        }
                        if (!cachedData.isNullOrEmpty()) {
                            flow.value = LearningState.Offline(cachedData, exception.message)
                        } else {
                            flow.value = LearningState.Error(exception.message ?: "Failed to load games", null)
                        }
                    }
                    result.map { }
                } finally {
                    gameRefreshMutex.withLock {
                        gameInFlight.remove(subcategoryId)
                    }
                }
            }
            gameInFlight[subcategoryId] = deferred
            deferred
        }
        return jobToAwait.await()
    }

    override fun getGamesForExplorer(explorerId: String): Flow<List<SubcategoryWithGames>> {
        return getGamesForExplorerState(explorerId).map { state ->
            when (state) {
                is LearningState.Cached -> state.data
                is LearningState.Fresh -> state.data
                is LearningState.Offline -> state.data
                is LearningState.Error -> state.cachedData ?: emptyList()
                is LearningState.Loading -> emptyList()
            }
        }
    }

    override fun getGamesForExplorerState(explorerId: String): Flow<LearningState<List<SubcategoryWithGames>>> {
        val flow = getOrCreateExplorerGameState(explorerId)
        repositoryScope.launch {
            ensureExplorerGamesInitialized(explorerId)
        }
        return flow.asStateFlow()
    }

    override suspend fun refreshGamesForExplorer(explorerId: String): Result<Unit> {
        val flow = getOrCreateExplorerGameState(explorerId)
        val jobToAwait = explorerGamesRefreshMutex.withLock {
            explorerGamesInFlight[explorerId]?.let { return@withLock it }
            val deferred = repositoryScope.async {
                try {
                    val result = remoteDataSource.fetchGamesForExplorer(explorerId)
                    result.onSuccess { fetched ->
                        val currentState = flow.value
                        val currentItems = when (currentState) {
                            is LearningState.Cached -> currentState.data
                            is LearningState.Fresh -> currentState.data
                            is LearningState.Offline -> currentState.data
                            is LearningState.Error -> currentState.cachedData ?: emptyList()
                            is LearningState.Loading -> emptyList()
                        }

                        val currentMap = currentItems.associateBy { it.subcategory.id }
                        val mergedItems = fetched.map { newItem ->
                            val oldItem = currentMap[newItem.subcategory.id]
                            val now = clock()
                            val expiryWindow = 24 * 3600 * 1000L
                            val safetyBuffer = 4 * 3600 * 1000L
                            val oldSub = oldItem?.subcategory
                            val newSub = newItem.subcategory
                            val isUrlValid = !oldSub?.imageUrl.isNullOrBlank() &&
                                    (now - oldSub.signedAtTimestamp < expiryWindow - safetyBuffer) &&
                                    oldSub.imageAssetId == newSub.imageAssetId

                            val subcategoryToUse = if (newSub.imageUrl.isNullOrBlank() && isUrlValid) {
                                newSub.copy(imageUrl = oldSub.imageUrl, signedAtTimestamp = oldSub.signedAtTimestamp)
                            } else {
                                newSub.copy(signedAtTimestamp = if (newSub.imageUrl != null) now else 0L)
                            }
                            newItem.copy(subcategory = subcategoryToUse)
                        }

                        flow.value = LearningState.Fresh(mergedItems)
                        try {
                            cacheStorage.saveGamesForExplorer(explorerId, mergedItems)
                        } catch (e: Exception) {
                            println("LearningRepository: Failed to persist explorer games cache: ${e.message}")
                        }
                    }.onFailure { exception ->
                        val currentState = flow.value
                        val cachedData = when (currentState) {
                            is LearningState.Cached -> currentState.data
                            is LearningState.Fresh -> currentState.data
                            is LearningState.Offline -> currentState.data
                            is LearningState.Error -> currentState.cachedData
                            is LearningState.Loading -> {
                                try { cacheStorage.loadGamesForExplorer(explorerId) } catch (_: Exception) { emptyList() }
                            }
                        }
                        if (!cachedData.isNullOrEmpty()) {
                            flow.value = LearningState.Offline(cachedData, exception.message)
                        } else {
                            flow.value = LearningState.Error(exception.message ?: "Failed to load games for explorer", null)
                        }
                    }
                    result.map { }
                } finally {
                    explorerGamesRefreshMutex.withLock {
                        explorerGamesInFlight.remove(explorerId)
                    }
                }
            }
            explorerGamesInFlight[explorerId] = deferred
            deferred
        }
        return jobToAwait.await()
    }

    override fun getQuestions(subcategoryId: String, gameId: String): Flow<List<Question>> {
        return getQuestionsState(subcategoryId, gameId).map { state ->
            when (state) {
                is LearningState.Cached -> state.data
                is LearningState.Fresh -> state.data
                is LearningState.Offline -> state.data
                is LearningState.Error -> state.cachedData ?: emptyList()
                is LearningState.Loading -> emptyList()
            }
        }
    }

    override fun getQuestionsState(subcategoryId: String, gameId: String): Flow<LearningState<List<Question>>> {
        val key = "${subcategoryId}_${gameId}"
        val flow = getOrCreateQuestionState(key)
        repositoryScope.launch {
            ensureQuestionsInitialized(subcategoryId, gameId)
        }
        return flow.asStateFlow()
    }

    override suspend fun refreshQuestions(subcategoryId: String, gameId: String): Result<Unit> {
        val key = "${subcategoryId}_${gameId}"
        val flow = getOrCreateQuestionState(key)
        val jobToAwait = questionRefreshMutex.withLock {
            questionInFlight[key]?.let { return@withLock it }
            val deferred = repositoryScope.async {
                try {
                    val result = remoteDataSource.fetchQuestions(subcategoryId, gameId)
                    result.onSuccess { fetched ->
                        val activeQuestions = fetched.filter { !it.isDraft }
                        flow.value = LearningState.Fresh(activeQuestions)
                        try {
                            cacheStorage.saveQuestions(subcategoryId, gameId, activeQuestions)
                        } catch (e: Exception) {
                            println("LearningRepository: Failed to persist questions cache: ${e.message}")
                        }
                    }.onFailure { exception ->
                        val currentState = flow.value
                        val cachedData = when (currentState) {
                            is LearningState.Cached -> currentState.data
                            is LearningState.Fresh -> currentState.data
                            is LearningState.Offline -> currentState.data
                            is LearningState.Error -> currentState.cachedData
                            is LearningState.Loading -> {
                                try { cacheStorage.loadQuestions(subcategoryId, gameId) } catch (_: Exception) { emptyList() }
                            }
                        }
                        if (!cachedData.isNullOrEmpty()) {
                            flow.value = LearningState.Offline(cachedData, exception.message)
                        } else {
                            flow.value = LearningState.Error(exception.message ?: "Failed to load questions", null)
                        }
                    }
                    result.map { }
                } finally {
                    questionRefreshMutex.withLock {
                        questionInFlight.remove(key)
                    }
                }
            }
            questionInFlight[key] = deferred
            deferred
        }
        return jobToAwait.await()
    }

    override fun preloadSubcategories(explorerId: String) {
        repositoryScope.launch {
            ensureSubcategoriesInitialized(explorerId)
        }
    }

    override fun preloadGames(subcategoryId: String) {
        repositoryScope.launch {
            ensureGamesInitialized(subcategoryId)
        }
    }

    override fun preloadGamesForExplorer(explorerId: String) {
        repositoryScope.launch {
            ensureExplorerGamesInitialized(explorerId)
        }
    }

    override fun preloadQuestions(subcategoryId: String, gameId: String) {
        repositoryScope.launch {
            ensureQuestionsInitialized(subcategoryId, gameId)
        }
    }
}
