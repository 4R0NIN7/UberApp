package com.untitledkingdom.ueberapp.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)

fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) ==
        PackageManager.PERMISSION_GRANTED
}

fun BluetoothGatt.printGattTable() {
    if (services.isEmpty()) {
        Timber.d("No service and characteristic available, call discoverServices() first?")
        return
    }
    services.forEach { service ->
        val characteristicsTable = service.characteristics.joinToString(
            separator = "\n|--",
            prefix = "|--"
        ) { it.uuid.toString() }
        Timber.d(
            "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
        )
    }
}

fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

@SuppressLint("MissingPermission")
fun Advertisement.toScannedDevice() = ScannedDevice(
    address = address,
    name = name ?: "Unknown",
    rssi = rssi
)
