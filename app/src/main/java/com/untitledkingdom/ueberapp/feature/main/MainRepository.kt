package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData(serviceUUID: String)
    fun getDataFilteredByDate(dateYYYYMMDD: String): Flow<RepositoryStatus>
    fun getLastDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus>
    fun getCharacteristicsPerDay(): Flow<RepositoryStatus>
}
