package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetHomeDataUseCase(
    private val repository: HomeRepository
) {
    operator fun invoke(): Flow<HomeData> {
        return repository.getHomeData()
    }
}
