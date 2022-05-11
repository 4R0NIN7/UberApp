package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.AppModules
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.toDeviceReading
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
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
    private val incrementer = AtomicInteger(1)
    private val numberOfTries = (incrementer.get().toFloat() * 19f).toInt()
    private val firstIdSent: MutableStateFlow<Int> = MutableStateFlow(0)
    private val lastIdSent: MutableStateFlow<Int> = MutableStateFlow(0)
    private var isFirstTime = true

    override suspend fun wipeData(serviceUUID: String) {
        database.getDao().wipeData(serviceUUID)
    }

    private fun setLastId(newId: Int) {
        lastIdSent.value = newId
    }

    private fun setFirstId(newId: Int) {
        firstIdSent.value = newId
    }

    private fun sendData(data: List<BleDataEntity>) {
        Timber.d("Size of data ${data.size}\nFirst id is ${data.first().id}\nLast id is ${data.last().id}")
        scope.launch {
            Timber.d("Sending data...")
            try {
                val response = apiService.sendDataToService(bleDatumEntities = data)
                if (response.isSuccessful) {
                    Timber.d("Data sent!")
                    setLastId(data.last().id)
                    database.getDao().saveAllData(
                        dataList = data.map {
                            BleDataEntity(
                                id = it.id,
                                temperature = it.temperature,
                                humidity = it.humidity,
                                dateTime = it.dateTime,
                                serviceUUID = it.serviceUUID,
                                isSynchronized = true
                            )
                        }
                    )
                } else {
                    throw Exception()
                }
            } catch (e: Exception) {
                Timber.d("Unable to send data!")
                incrementer.incrementAndGet()
            }
        }
    }

    private suspend fun sendDataToServer(serviceUUID: String) {
        val data = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (isFirstTime) {
            isFirstTime = false
            setFirstId(data.first().id)
            setLastId(apiService.getLastSynchronizedReading())
        }
        if (lastIdSent.value + numberOfTries == data.last().id) {
            sendData(
                data.filter {
                    it.id in lastIdSent.value..data.last().id
                }
            )
        }
    }

    override suspend fun saveData(serviceUUID: String, reading: Reading) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleDataEntity = BleDataEntity(
            temperature = reading.temperature,
            humidity = reading.humidity,
            dateTime = now,
            serviceUUID = serviceUUID,
        )
        database.getDao().saveData(data = bleDataEntity)
        sendDataToServer(serviceUUID)
    }

    override fun getDataFilteredByDate(dateYYYYMMDD: String): Flow<RepositoryStatus> =
        database.getDao().getDataFilteredByDate(dateYYYYMMDD).map { list ->
            RepositoryStatus.SuccessGetListBleData(list.map { it.toDeviceReading() })
        }.catch {
            emit(RepositoryStatus.SuccessGetListBleData(listOf()))
        }

    override fun getDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus> =
        database.getDao().getAllDataFlow(serviceUUID).distinctUntilChanged().map { list ->
            RepositoryStatus.SuccessGetListBleData(list.map { it.toDeviceReading() })
        }

    override fun getLastDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus> =
        database.getDao().getLastBleData(serviceUUID).map { data ->
            try {
                RepositoryStatus.SuccessBleData(data.toDeviceReading())
            } catch (e: Exception) {
                RepositoryStatus.SuccessBleData(data = null)
            }
        }

    override fun getCharacteristicsPerDay(): Flow<RepositoryStatus> =
        database.getDao().getAnalyticsPerDayFromDataBase().map { listAnalytics ->
            RepositoryStatus.SuccessBleCharacteristics(listAnalytics)
        }

    override fun stop() {
        scope.cancel()
    }
}
