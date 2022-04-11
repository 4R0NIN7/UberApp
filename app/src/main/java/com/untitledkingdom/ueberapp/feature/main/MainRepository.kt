package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData()
    fun sendData()
    fun startReadingDataFromDevice(
        characteristic: String,
        serviceUUID: String
    ): Flow<RepositoryStatus>
    fun stopReadingDataFromDevice()
    suspend fun readOnceFromDevice(fromService: String, fromCharacteristic: String)
    suspend fun writeDateToDevice(service: String, characteristic: String)
    suspend fun getDataFromDatabase(serviceUUID: String): List<BleData>
}
