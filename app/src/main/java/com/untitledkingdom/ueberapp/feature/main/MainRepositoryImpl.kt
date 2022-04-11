package com.untitledkingdom.ueberapp.feature.main

import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.ScanParameters
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.Readings
import com.untitledkingdom.ueberapp.feature.main.data.MainRepositoryConst
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.generateRandomHumidity
import com.untitledkingdom.ueberapp.utils.functions.generateRandomTemperature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
    private val scope: CoroutineScope,
    private val dataStorage: DataStorage
) : MainRepository {
    private var isReading = true
    private suspend fun saveToDataBase(
        value: Readings,
        serviceUUID: String
    ) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleData = BleData(
            data = value,
            localDateTime = now,
            serviceUUID = serviceUUID,
        )
        database.getDao().saveData(data = bleData)
        Timber.d("Saved to dataBase")
    }

    override suspend fun getDataFromDatabase(serviceUUID: String): List<BleData> {
        val values = database
            .getDao()
            .getAllData()
            .filter { it.serviceUUID == serviceUUID }
        if (values.size % 20 == 0) {
            sendData()
        }
        return values
    }

    override suspend fun wipeData() {
        database.getDao().wipeData()
    }

    override fun sendData() {
        Timber.d("sendData - There's 20 records!")
    }

    override fun startReadingDataFromDevice(): Flow<RepositoryStatus> = flow {
        Timber.d("Starting reading in Repository")
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        val device = ScanParameters(device = peripheral)
        emit(RepositoryStatus.Loading(getDataFromDatabase(device.serviceUUID)))
        try {
            while (true) {
                device.write(value = generateRandomTemperature(), DeviceConst.TEMPERATURE)
                device.write(value = generateRandomHumidity(), DeviceConst.HUMIDITY)
                delay(MainRepositoryConst.DELAY)
                val temperature = device.read(DeviceConst.TEMPERATURE).replace(',', '.')
                val humidity = device.read(DeviceConst.HUMIDITY).replace(',', '.')
                val newReading = Readings(
                    temperature = temperature.toDouble(),
                    humidity = humidity.toDouble()
                )
                saveToDataBase(value = newReading, serviceUUID = device.serviceUUID)
                val dataFromDataBase = getDataFromDatabase(
                    serviceUUID = device.serviceUUID
                )
                emit(RepositoryStatus.Success(dataFromDataBase))
            }
        } catch (e: Exception) {
            throw e
        }
    }.onStart {
        isReading = true
    }.takeWhile {
        isReading
    }.onCompletion {
        isReading = true
    }.catch { cause ->
        Timber.d(cause)
    }

    override fun stopReadingDataFromDevice() {
        Timber.d("Stopping reading in Repository")
        isReading = false
    }

    override suspend fun readOnceFromDevice() {
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        val device = ScanParameters(device = peripheral)
        try {
            device.write(value = generateRandomTemperature(), DeviceConst.TEMPERATURE)
            device.write(value = generateRandomHumidity(), DeviceConst.HUMIDITY)
            val temperature = device.read(DeviceConst.TEMPERATURE).replace(',', '.')
            val humidity = device.read(DeviceConst.HUMIDITY).replace(',', '.')
            val newReading = Readings(
                temperature = temperature.toDouble(),
                humidity = humidity.toDouble()
            )
            saveToDataBase(value = newReading, serviceUUID = device.serviceUUID)
            getDataFromDatabase(serviceUUID = device.serviceUUID)
        } catch (e: Exception) {
            throw e
        }
    }
}
