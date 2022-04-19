package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
    private val apiService: ApiService
) : MainRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastIdSent = 0
    private var isFirstTime = true
    override suspend fun getData(serviceUUID: String): List<BleData> {
        val data = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (lastIdSent + 20 >= data.last().id) {
            sendData(data)
            isFirstTime = false
            lastIdSent = data.last().id
        }
        return data
    }

    override suspend fun wipeData() {
        database.getDao().wipeData()
    }

    private fun sendData(data: List<BleData>) {
        scope.launch {
            Timber.d("Sending data...")
            try {
                val response = apiService.sendDataToService(bleData = data)
                if (response.isSuccessful) {
                    Timber.d("Data sent!")
                } else {
                    throw Exception()
                }
            } catch (e: Exception) {
                Timber.d("Unable to send data!")
            }
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
    }

    override fun getDataFromDataBaseAsFlow(serviceUUID: String): Flow<RepositoryStatus> =
        flow {
            database.getDao().getAllDataFlow().distinctUntilChanged().collect { data ->
                if (isFirstTime || lastIdSent + 20 == data.last().id) {
                    sendData(data)
                    isFirstTime = false
                    lastIdSent = data.last().id
                }
                emit(RepositoryStatus.SuccessBleData(data))
            }
        }

    override fun clear() {
        scope.cancel()
    }
}
