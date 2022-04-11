package com.untitledkingdom.ueberapp.feature.main

import ReadingsOuterClass
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.uByteArray
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

@ExperimentalUnsignedTypes
class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
    private val scope: CoroutineScope,
    private val dataStorage: DataStorage
) : MainRepository {
    private var isReading = true
    private suspend fun saveToDataBase(
        value: DeviceReading,
        serviceUUID: String
    ) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleData = BleData(
            deviceReading = value,
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

    override suspend fun startReadingDataFromDevice(
        characteristic: String,
        serviceUUID: String
    ) {
        Timber.d("Starting reading in Repository")
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        peripheral.connect()
        peripheral
            .observe(characteristic = characteristicOf(serviceUUID, characteristic))
            .collect { data ->
                val reading = ReadingsOuterClass.Readings.parseFrom(data)
                Timber.d("Reading is temperature = ${reading.temperature}, humidity = ${reading.hummidity}")
                saveToDataBase(DeviceReading(reading.temperature, reading.hummidity), serviceUUID)
            }
        peripheral.disconnect()
    }

    override fun stopReadingDataFromDevice() {
        Timber.d("Stopping reading in Repository")
        isReading = false
    }

    override suspend fun readOnceFromDevice(service: String, characteristic: String) {
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        val device = Device(device = peripheral)
        try {
            val data = device.read(fromService = service, fromCharacteristic = characteristic)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun writeDateToDevice(
        service: String,
        characteristic: String,
    ) {
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        val device = Device(device = peripheral)
        try {
            val currentDate = timeManager.provideCurrentLocalDateTime()
            val data = device.read(
                fromCharacteristic = characteristic,
                fromService = service
            )
            val dateFromDevice = toDateString(data)
            if (!checkIfDateIsTheSame(
                    date = currentDate,
                    dateFromDevice = dateFromDevice
                )
            ) {
                Timber.d("writeDateToDevice Saving date")
                device.write(currentDate.uByteArray(), service, characteristic)
            }
        } catch (e: Exception) {
            Timber.d("Unable to write deviceReading")
        }
    }

    private fun checkIfDateIsTheSame(dateFromDevice: String, date: LocalDateTime): Boolean {
        val dateFromLocalDateTime = "${date.dayOfMonth}${date.monthValue}${date.year}"
        return dateFromDevice == dateFromLocalDateTime
    }
}
