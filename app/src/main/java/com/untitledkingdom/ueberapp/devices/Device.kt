package com.untitledkingdom.ueberapp.devices

import ReadingsOuterClass
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class Device @Inject constructor(
    private val dataStorage: DataStorage
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var device: Peripheral? = null
    private suspend fun getDevice(): Peripheral {
        return device
            ?: scope.peripheral(
                dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS)
            ).also { device = it }
    }

    suspend fun read(fromService: String, fromCharacteristic: String): DeviceStatus {
        try {
            getDevice().connect()
            getDevice().services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    withContext(Dispatchers.IO) {
                        val dataBytes = getDevice().read(
                            characteristicOf(
                                service = fromService,
                                characteristic = fromCharacteristic
                            )
                        )
                        val readings = ReadingsOuterClass.Readings.parseFrom(dataBytes)
                        return@withContext DeviceStatus.SuccessDeviceReading(
                            DeviceReading(readings.temperature, readings.hummidity)
                        )
                    }
                }
            }
            return DeviceStatus.Error
        } catch (e: Exception) {
            Timber.d("Exception in read! + $e")
            throw e
        } finally {
            getDevice().disconnect()
        }
    }

    suspend fun readDate(fromService: String, fromCharacteristic: String): DeviceStatus {
        try {
            getDevice().connect()
            var date: ByteArray = byteArrayOf()
            getDevice().services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    date = getDevice().read(
                        characteristicOf(
                            service = fromService,
                            characteristic = fromCharacteristic
                        )
                    )
                }
            }
            return DeviceStatus.SuccessDate(date = date.toList())
        } catch (e: Exception) {
            Timber.d("Exception in read! + $e")
            throw e
        } finally {
            getDevice().disconnect()
        }
    }

    suspend fun write(value: ByteArray, toService: String, toCharacteristic: String) {
        try {
            getDevice().connect()
            getDevice().services?.first {
                it.serviceUuid == UUID.fromString(toService)
            }?.characteristics?.forEach { it ->
                if (it.characteristicUuid == UUID.fromString(toCharacteristic)) {
                    getDevice().write(
                        characteristicOf(
                            service = toService,
                            characteristic = toCharacteristic
                        ),
                        value,
                        writeType = WriteType.WithResponse
                    )
                }
            }
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
        } finally {
            getDevice().disconnect()
        }
    }

    fun observationOnDataCharacteristic(): Flow<ByteArray> = flow {
        emit(getDevice())
    }.flatMapConcat { peripheral ->
        peripheral.observe(
            characteristicOf(
                service = DeviceConst.SERVICE_DATA_SERVICE,
                characteristic = DeviceConst.READINGS_CHARACTERISTIC
            )
        )
    }

    fun disconnect() {
        scope.cancel("Device disconnected!")
    }
}

sealed class DeviceStatus {
    data class SuccessDeviceReading(val reading: DeviceReading) : DeviceStatus()
    data class SuccessDate(val date: List<Byte>) : DeviceStatus()
    object Error : DeviceStatus()
}
