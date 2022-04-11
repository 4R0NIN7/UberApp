package com.untitledkingdom.ueberapp.devices

import android.content.res.Resources
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            throw Resources.NotFoundException()
        } catch (e: Resources.NotFoundException) {
            Timber.d("deviceRead There are no characteristics!")
            return byteArrayOf()
        } catch (e: Exception) {
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
        } finally {
            device.disconnect()
        }
    }

    suspend fun observationOnCharacteristic(service: String, characteristic: String): Flow<String> =
        flow {
            try {
                device.observe(
                    characteristic = characteristicOf(
                        service = service,
                        characteristic = characteristic
                    )
                ).collect {
                    emit(String(it))
                }
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
}
