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
    override suspend fun saveToDataBase(value: String) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleData = BleData(data = value, date = now)
        database.getDao().saveData(data = bleData)
        Timber.d("Saved to dataBase")
    }

    override fun sendData() {
        TODO("Not implemented yet")
    }
}
