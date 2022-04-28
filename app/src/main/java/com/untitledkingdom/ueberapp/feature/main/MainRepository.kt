package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData()
    suspend fun saveData(serviceUUID: String, deviceReading: DeviceReading)
    fun getDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus>
    fun stop()
    val lastIdSent: Flow<Int>
    val firstIdSent: Flow<Int>
}
