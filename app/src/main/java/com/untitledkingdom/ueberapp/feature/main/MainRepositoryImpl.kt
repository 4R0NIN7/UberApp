package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.AppModules
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
    private val apiService: ApiService,
    @AppModules.IoDispatcher private val dispatcher: CoroutineDispatcher
) : MainRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastIdSent = 0
    private var isFirstTime = true
    override suspend fun getData(serviceUUID: String): List<BleData> {
        val data = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (lastIdSent + 19 == data.last().id) {
            sendData(
                data.filter {
                    it.id in lastIdSent..data.last().id
                }
            )
            isFirstTime = false
            lastIdSent = data.last().id
        }
        return data
    }

    override suspend fun wipeData() {
        database.getDao().wipeData()
    }

    private fun sendData(data: List<BleData>) {
        Timber.d("Size of data ${data.size}\nFirst id is ${data.first().id}\nLast id is ${data.last().id}")
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
        database.getDao().getAllDataFlow().distinctUntilChanged().map { data ->
            if (isFirstTime) {
                sendData(data)
                lastIdSent = data.last().id
                isFirstTime = false
            }
            if (lastIdSent + 19 == data.last().id) {
                sendData(
                    data.filter {
                        it.id in lastIdSent..data.last().id
                    }
                )
                lastIdSent = data.last().id
            }
            RepositoryStatus.SuccessBleData(data)
        }

    override fun clear() {
        scope.cancel()
    }
}
