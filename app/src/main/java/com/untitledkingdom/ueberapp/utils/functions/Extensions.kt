package com.untitledkingdom.ueberapp.utils.functions

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import okhttp3.internal.and
import timber.log.Timber
import java.nio.ByteBuffer
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)

fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

fun Advertisement.toScannedDevice() = ScannedDevice(
    address = address,
    name = name ?: "Unknown name",
    rssi = rssi
)

fun Dp.toPx(density: Density) = value * density.density
fun BleDataEntity.toDeviceReading() = DeviceReading(
    id = id,
    reading = Reading(temperature, humidity),
    localDateTime = dateTime,
    isSynchronized = isSynchronized
)

@ExperimentalUnsignedTypes
fun LocalDateTime.toUByteArray(): ByteArray {
    val yearBytes = yearToByteArray(year = year)
    return arrayOf(
        dayOfMonth.toUByte(),
        monthValue.toUByte(),
        yearBytes[3].toUByte(),
        yearBytes[2].toUByte()
    ).toUByteArray().toByteArray()
}

private fun yearToUBytesStringVersion(year: Int): List<UByte> {
    val yearBinaryString = Integer.toBinaryString(year)
    val resultWithPadZero = String.format("%32s", yearBinaryString).replace(" ", "0")
    val chunked = resultWithPadZero.chunked(8)
    return try {
        val bytes = chunked.map {
            it.toUByte(2)
        }
        Timber.d("yearToUBytes Bytes are $bytes")
        val zero: UByte = Integer.valueOf("0").toUByte()
        bytes.filter {
            it > zero
        }
    } catch (e: Exception) {
        Timber.d("exception $e")
        listOf()
    }
}

private fun uBytesToYearStringVersion(byte1: Byte, byte2: Byte): Int {
    val byte1ToBits = String.format("%8s", Integer.toBinaryString(byte1 and 0xFF)).replace(' ', '0')
    val byte2ToBits = String.format("%8s", Integer.toBinaryString(byte2 and 0xFF)).replace(' ', '0')
    val concat = byte2ToBits.plus(byte1ToBits)
    return Integer.parseInt(concat, 2)
}

private fun yearToByteArray(year: Int): ByteArray {
    return ByteBuffer.allocate(4).putInt(year).array()
}
