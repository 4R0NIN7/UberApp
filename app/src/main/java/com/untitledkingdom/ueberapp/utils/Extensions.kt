package com.untitledkingdom.ueberapp.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import timber.log.Timber

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
fun ScanResult.toScannedDevice() = ScannedDevice(
    address = device.address,
    name = scanRecord?.deviceName ?: device.name ?: "Unknown",
    rssi = rssi
)
