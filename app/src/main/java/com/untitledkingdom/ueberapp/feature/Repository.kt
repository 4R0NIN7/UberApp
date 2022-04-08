package com.untitledkingdom.ueberapp.feature

import com.untitledkingdom.ueberapp.feature.data.BleData

interface Repository {
    suspend fun saveToDataBase(value: String)
    suspend fun getDataFromDataBase(): List<BleData>
    fun sendData()
}
