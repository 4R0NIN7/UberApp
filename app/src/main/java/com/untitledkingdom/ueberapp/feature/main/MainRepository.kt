package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.devices.data.BleData

interface MainRepository {
    suspend fun wipeData()
    fun sendData()
    suspend fun startReadingDataFromDevice(
        characteristic: String,
        serviceUUID: String
    )

    fun stopReadingDataFromDevice()
    suspend fun readOnceFromDevice(service: String, characteristic: String)
    suspend fun writeDateToDevice(service: String, characteristic: String)
    suspend fun getDataFromDatabase(serviceUUID: String): List<BleData>
}
