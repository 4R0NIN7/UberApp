package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.BleData
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData()
    fun sendData()
    fun startReadingDataFromDevice(): Flow<RepositoryStatus>
    fun stopReadingDataFromDevice()
    suspend fun readOnceFromDevice()
}

sealed class RepositoryStatus {
    data class Success(val data: List<BleData>) : RepositoryStatus()
    object Error : RepositoryStatus()
}
