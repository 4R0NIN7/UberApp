package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.data.MainRepositoryConst
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
) : MainRepository {

    override suspend fun getData(serviceUUID: String): List<BleData> {
        val data = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (data.size % 20 == 0) {
            sendData()
        }
        return data
    }

    override suspend fun wipeData() {
        database.getDao().wipeData()
    }

    override suspend fun sendData() {
        Timber.d("Sending data...")
        delay(MainRepositoryConst.DELAY_API)
        Timber.d("Data sent!")
    }

    override suspend fun saveData(serviceUUID: String, deviceReading: DeviceReading) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleData = BleData(
            deviceReading = deviceReading,
            localDateTime = now,
            serviceUUID = serviceUUID,
        )
        database.getDao().saveData(data = bleData)
        Timber.d("Saved to dataBase")
    }

    override fun getDataFromDataBaseAsFlow(serviceUUID: String): Flow<RepositoryStatus> =
        flow {
            val data = database
                .getDao()
                .getAllData()
                .filter { it.serviceUUID == serviceUUID }
            if (data.size % 20 == 0) {
                sendData()
            }
            emit(RepositoryStatus.SuccessBleData(data))
        }
}
