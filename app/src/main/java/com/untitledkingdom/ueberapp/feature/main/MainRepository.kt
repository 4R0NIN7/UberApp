package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData(serviceUUID: String)
    suspend fun saveData(serviceUUID: String, reading: Reading)
    fun getDataFilteredByDate(dateYYYYMMDD: String): Flow<RepositoryStatus>
    fun getDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus>
    fun getLastDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus>
    fun getCharacteristicsPerDay(): Flow<RepositoryStatus>
    fun stop()
}
