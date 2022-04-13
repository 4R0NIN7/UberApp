package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.data.MainRepositoryConst
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
) : MainRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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

    override fun sendData() {
        scope.launch {
            Timber.d("Sending data...")
            delay(MainRepositoryConst.DELAY_API)
            Timber.d("Data sent!")
        }
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
            database.getDao().getAllDataFlow().distinctUntilChanged().collect { data ->
                Timber.d("dataFromDataBase")
                if (data.size % 20 == 0) {
                    sendData()
                    Timber.d("I am not blocked!")
                }
                Timber.d("I am not blocked! beforce emit")
                emit(RepositoryStatus.SuccessBleData(data))
            }
        }

    override fun clear() {
        scope.cancel()
    }
}
