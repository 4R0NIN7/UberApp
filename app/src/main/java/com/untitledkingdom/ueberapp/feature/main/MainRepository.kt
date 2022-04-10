package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData()
    fun sendData()
    fun startReadingDataFromDevice(): Flow<RepositoryStatus>
    fun stopReadingDataFromDevice()
    suspend fun readOnceFromDevice()
    suspend fun getDataFromDatabase(serviceUUID: String): List<BleData>
}
