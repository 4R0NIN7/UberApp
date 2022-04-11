package com.untitledkingdom.ueberapp.devices

import ReadingsOuterClass
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@ExperimentalUnsignedTypes
class Device @Inject constructor(private val device: Peripheral) {
    suspend fun read(fromService: String, fromCharacteristic: String): ByteArray {
        try {
            device.connect()
            device.services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    return device.read(
                        characteristicOf(
                            service = fromService,
                            characteristic = fromCharacteristic
                        )
                    )
                }
            }
            return byteArrayOf()
        } catch (e: Exception) {
            Timber.d("Exception in read! + $e")
            return byteArrayOf()
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
        } finally {
            device.disconnect()
        }
    }

    suspend fun observationOnCharacteristic(): Flow<DeviceReading> =
        flow {
            try {
                device.observe(
                    characteristic = characteristicOf(
                        service = DeviceConst.SERVICE_DATA_SERVICE,
                        characteristic = DeviceConst.READINGS_CHARACTERISTIC
                    )
                ).collect { data ->
                    val reading = ReadingsOuterClass.Readings.parseFrom(data)
                    Timber.d("Reading is temperature = ${reading.temperature}, humidity = ${reading.hummidity}")
                    emit(
                        DeviceReading(
                            temperature = reading.temperature,
                            humidity = reading.hummidity
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.d(e)
            }
        }.onStart { device.connect() }.onCompletion { device.disconnect() }
}
