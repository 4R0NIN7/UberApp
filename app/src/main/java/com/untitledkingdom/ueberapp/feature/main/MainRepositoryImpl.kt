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
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val _firstIdSent: MutableStateFlow<Int> = MutableStateFlow(0)
    private val _lastIdSent: MutableStateFlow<Int> = MutableStateFlow(0)
    private var isFirstTime = true

    override val firstIdSent: Flow<Int>
        get() = _firstIdSent

    override val lastIdSent: Flow<Int>
        get() = _lastIdSent

    override suspend fun wipeData() {
        database.getDao().wipeData()
    }

    private fun setLastId(newId: Int) {
        _lastIdSent.value = newId
    }

    private fun setFirstId(newId: Int) {
        _firstIdSent.value = newId
    }

    private fun sendData(data: List<BleData>) {
        Timber.d("Size of data ${data.size}\nFirst id is ${data.first().id}\nLast id is ${data.last().id}")
        scope.launch {
            Timber.d("Sending data...")
            try {
                val response = apiService.sendDataToService(bleData = data)
                if (response.isSuccessful) {
                    Timber.d("Data sent!")
                    setLastId(data.last().id)
                } else {
                    throw Exception()
                }
            } catch (e: Exception) {
                Timber.d("Unable to send data!")
            }
        }
    }

    private suspend fun sendDataToServer(serviceUUID: String) {
        val data = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (isFirstTime) {
            sendData(data)
            isFirstTime = false
            setFirstId(data.first().id)
        }
        if (_lastIdSent.value + 19 == data.last().id) {
            sendData(
                data.filter {
                    it.id in _lastIdSent.value..data.last().id
                }
            )
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
        sendDataToServer(serviceUUID)
    }

    override fun getDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus> =
        database.getDao().getAllDataFlow().distinctUntilChanged().map { data ->
            RepositoryStatus.SuccessBleData(data)
        }

    override fun stop() {
        scope.cancel()
    }
}
