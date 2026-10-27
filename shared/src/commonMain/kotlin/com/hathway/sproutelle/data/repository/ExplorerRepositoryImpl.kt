package com.hathway.sproutelle.data.repository

import com.hathway.sproutelle.core.currentTimeMillis
import com.hathway.sproutelle.data.datasource.ExplorerCache
import com.hathway.sproutelle.data.datasource.ExplorerCacheStorage
import com.hathway.sproutelle.data.datasource.ExplorerRemoteDataSource
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.repository.ExplorerRepository
import com.hathway.sproutelle.domain.repository.ExplorerState
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

class ExplorerRepositoryImpl(
    private val remoteDataSource: ExplorerRemoteDataSource,
    private val explorerCache: ExplorerCacheStorage = ExplorerCache(),
    private val clock: () -> Long = { currentTimeMillis() }
) : ExplorerRepository {

    private val _repositoryState = MutableStateFlow<ExplorerState<List<ExplorerItem>>>(ExplorerState.Loading)
    private val refreshMutex = Mutex()
    private val initMutex = Mutex()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isInitialized = false
    private var inFlightRefresh: Deferred<Result<Unit>>? = null

    init {
        repositoryScope.launch {
            ensureInitialized()
        }
    }

    private suspend fun ensureInitialized() {
        if (isInitialized) return
        initMutex.withLock {
            if (!isInitialized) {
                try {
                    val cached = explorerCache.loadCachedItems()
                    if (cached.isNotEmpty()) {
                        _repositoryState.value = ExplorerState.Cached(cached)
                    }
                } catch (e: Exception) {
                    println("ExplorerRepository: Asynchronous cache load error: ${e.message}")
                }
                isInitialized = true
                // Warm-start / cold-start: Always start background refresh directly in this coroutine context
                refreshExplorerItems()
            }
        }
    }

    override fun getExplorerItems(): Flow<List<ExplorerItem>> {
        return getExplorerState().map { state ->
            when (state) {
                is ExplorerState.Cached -> state.data
                is ExplorerState.Fresh -> state.data
                is ExplorerState.Offline -> state.data
                is ExplorerState.Error -> state.cachedData ?: emptyList()
                is ExplorerState.Loading -> emptyList()
            }
        }
    }

    override fun getExplorerState(): Flow<ExplorerState<List<ExplorerItem>>> {
        return _repositoryState.asStateFlow()
    }

    override suspend fun refreshExplorerItems(): Result<Unit> {
        if (!isInitialized) {
            ensureInitialized()
        }
        val jobToAwait = refreshMutex.withLock {
            inFlightRefresh?.let { return@withLock it }
            val deferred = repositoryScope.async {
                try {
                    val result = remoteDataSource.fetchExplorerItemsFromSupabase()
                    result.onSuccess { fetchedItems ->
                        val activeItems = fetchedItems.filter { it.isActive }
                        val currentState = _repositoryState.value
                        val currentItems = when (currentState) {
                            is ExplorerState.Cached -> currentState.data
                            is ExplorerState.Fresh -> currentState.data
                            is ExplorerState.Offline -> currentState.data
                            is ExplorerState.Error -> currentState.cachedData ?: emptyList()
                            is ExplorerState.Loading -> emptyList()
                        }

                        // Retain valid cached signed URLs / asset references if signing failed temporarily
                        val currentMap = currentItems.associateBy { it.id }
                        val mergedItems = activeItems.map { newItem ->
                            val oldItem = currentMap[newItem.id]
                            val now = clock()
                            val expiryWindow = 24 * 3600 * 1000L
                            val safetyBuffer = 4 * 3600 * 1000L // 20 hours validity
                            val isUrlValid = !oldItem?.imageUrl.isNullOrBlank() &&
                                    (now - oldItem.signedAtTimestamp < expiryWindow - safetyBuffer) &&
                                    oldItem.titleImageAssetId == newItem.titleImageAssetId

                            if (newItem.imageUrl.isNullOrBlank() && isUrlValid) {
                                newItem.copy(imageUrl = oldItem.imageUrl, signedAtTimestamp = oldItem.signedAtTimestamp)
                            } else {
                                newItem.copy(signedAtTimestamp = if (newItem.imageUrl != null) now else 0L)
                            }
                        }

                        _repositoryState.value = ExplorerState.Fresh(mergedItems)
                        try {
                            explorerCache.saveCachedItems(mergedItems)
                        } catch (e: Exception) {
                            println("ExplorerRepository: Failed to persist cache: ${e.message}")
                        }
                    }.onFailure { exception ->
                        val currentState = _repositoryState.value
                        val cachedData = when (currentState) {
                            is ExplorerState.Cached -> currentState.data
                            is ExplorerState.Fresh -> currentState.data
                            is ExplorerState.Offline -> currentState.data
                            is ExplorerState.Error -> currentState.cachedData
                            is ExplorerState.Loading -> {
                                try { explorerCache.loadCachedItems() } catch (_: Exception) { emptyList() }
                            }
                        }
                        if (!cachedData.isNullOrEmpty()) {
                            _repositoryState.value = ExplorerState.Offline(cachedData, exception.message)
                        } else {
                            _repositoryState.value = ExplorerState.Error(exception.message ?: "Failed to load categories", null)
                        }
                    }
                    result.map { }
                } finally {
                    refreshMutex.withLock {
                        inFlightRefresh = null
                    }
                }
            }
            inFlightRefresh = deferred
            deferred
        }
        return jobToAwait.await()
    }
}
