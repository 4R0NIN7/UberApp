package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun wipeData()
    fun sendData()
    suspend fun saveData(serviceUUID: String, deviceReading: DeviceReading)
    suspend fun getData(serviceUUID: String): List<BleData>
    fun getDataFromDataBaseAsFlow(serviceUUID: String): Flow<RepositoryStatus>
    fun clear()
}
