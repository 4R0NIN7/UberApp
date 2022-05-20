package com.untitledkingdom.ueberapp.background

import com.untitledkingdom.ueberapp.devices.data.Reading

interface ReadingRepository {
    suspend fun saveData(serviceUUID: String, reading: Reading)
    fun start(serviceUUID: String)
    fun stop()
}
