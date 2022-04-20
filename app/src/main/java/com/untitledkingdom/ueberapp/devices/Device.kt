package com.untitledkingdom.ueberapp.devices

import ReadingsOuterClass
import com.juul.kable.ConnectionLostException
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class Device @Inject constructor(
    private val device: Peripheral,
) {

    suspend fun read(fromService: String, fromCharacteristic: String): DeviceDataStatus {
        try {
            device.services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    var readings: ReadingsOuterClass.Readings?
                    withContext(Dispatchers.IO) {
                        val dataBytes = device.read(
                            characteristicOf(
                                service = fromService,
                                characteristic = fromCharacteristic
                            )
                        )
                        readings = ReadingsOuterClass.Readings.parseFrom(dataBytes)
                    }
                    if (readings != null) {
                        return DeviceDataStatus.SuccessDeviceDataReading(
                            DeviceReading(readings!!.temperature, readings!!.hummidity)
                        )
                    }
                }
            }
            return DeviceDataStatus.Error
        } catch (e: Exception) {
            Timber.d("Exception in read! + $e")
            throw e
        }
    }

    suspend fun readDate(fromService: String, fromCharacteristic: String): DeviceDataStatus {
        try {
            device.connect()
            var date: ByteArray = byteArrayOf()
            device.services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    date = device.read(
                        characteristicOf(
                            service = fromService,
                            characteristic = fromCharacteristic
                        )
                    )
                }
            }
            return DeviceDataStatus.SuccessDate(date = date.toList())
        } catch (e: ConnectionLostException) {
            Timber.d("Exception in read! + $e")
            throw e
        } finally {
            device.disconnect()
        }
    }

    suspend fun write(value: ByteArray, toService: String, toCharacteristic: String) {
        try {
            device.connect()
            device.services?.first {
                it.serviceUuid == UUID.fromString(toService)
            }?.characteristics?.forEach { it ->
                if (it.characteristicUuid == UUID.fromString(toCharacteristic)) {
                    device.write(
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
            throw e
        } finally {
            device.disconnect()
        }
    }

    fun observationOnDataCharacteristic(): Flow<DeviceReading> = flow {
        try {
            device.observe(
                characteristicOf(
                    service = DeviceConst.SERVICE_DATA_SERVICE,
                    characteristic = DeviceConst.READINGS_CHARACTERISTIC
                )
            ).collect { data ->
                val reading = ReadingsOuterClass.Readings.parseFrom(data)
                emit(DeviceReading(reading.temperature, reading.hummidity))
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Device disconnected!")
            throw e
        } catch (e: Exception) {
            Timber.d("Device disconnected error! +$e")
        }
    }
}

sealed class DeviceDataStatus {
    data class SuccessDeviceDataReading(val reading: DeviceReading) : DeviceDataStatus()
    data class SuccessDate(val date: List<Byte>) : DeviceDataStatus()
    object Error : DeviceDataStatus()
}
