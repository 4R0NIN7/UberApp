package com.untitledkingdom.ueberapp.service

import com.untitledkingdom.ueberapp.devices.data.Reading

interface ReadingRepository {
    suspend fun saveData(serviceUUID: String, reading: Reading)
    fun start()
}
