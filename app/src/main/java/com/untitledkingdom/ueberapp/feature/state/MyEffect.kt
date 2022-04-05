package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult

sealed interface MyEffect {
    object ScanDevices : MyEffect
    object StopScanDevices : MyEffect
    data class ConnectToDevice(val scanResult: ScanResult) : MyEffect
    data class DisconnectFromDevice(val gatt: BluetoothGatt) : MyEffect
}
