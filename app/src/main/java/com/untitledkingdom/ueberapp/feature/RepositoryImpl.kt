package com.untitledkingdom.ueberapp.feature

import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.feature.data.BleData
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import timber.log.Timber
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager
) : Repository {
    override suspend fun saveToDataBase(
        value: String,
        characteristicUUID: String,
        serviceUUID: String
    ) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleData = BleData(
            data = value,
            localDateTime = now,
            serviceUUID = serviceUUID,
            characteristicUUID = characteristicUUID
        )
        database.getDao().saveData(data = bleData)
        Timber.d("Saved to dataBase")
    }

    override suspend fun getDataFromDataBase(
        serviceUUID: String,
        characteristicUUID: String
    ): List<BleData> = database
        .getDao()
        .getAllData()
        .filter { it.serviceUUID == serviceUUID && it.characteristicUUID == characteristicUUID }

    override suspend fun wipeData() = database.getDao().wipeData()

    override fun sendData() {
        Timber.d("sendData - There's 20 records!")
    }
}
