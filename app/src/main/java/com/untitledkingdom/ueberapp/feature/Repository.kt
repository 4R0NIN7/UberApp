package com.untitledkingdom.ueberapp.feature

import com.untitledkingdom.ueberapp.feature.data.BleData

interface Repository {
    suspend fun saveToDataBase(
        value: String,
        characteristicUUID: String,
        serviceUUID: String
    )

    suspend fun getDataFromDataBase(
        serviceUUID: String,
        characteristicUUID: String
    ): List<BleData>
    suspend fun wipeData()
    fun sendData()
}
