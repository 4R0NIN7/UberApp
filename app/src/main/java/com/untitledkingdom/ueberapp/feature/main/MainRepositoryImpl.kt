package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.api.ApiConst
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
    private val apiService: ApiService
) : MainRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override suspend fun getData(serviceUUID: String): List<BleData> {
        val data = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (data.size % 20 == 0) {
            sendData(data)
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
                val response = Response.success<ResponseBody>(
                    ApiConst.HTTP_OK,
                    "".toResponseBody("".toMediaTypeOrNull())
                )
                /* If there were a server then */
                // val response = apiService.sendDataToService(bleData = data)
                delay(MainRepositoryConst.DELAY_API)
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
                if (data.size % 20 == 0) {
                    sendData(data)
                }
                emit(RepositoryStatus.SuccessBleData(data))
            }
        }

    override fun clear() {
        scope.cancel()
    }
}
