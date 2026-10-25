package com.hathway.sproutelle.domain.repository

import com.hathway.sproutelle.domain.model.HomeData
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHomeData(): Flow<HomeData>
}
