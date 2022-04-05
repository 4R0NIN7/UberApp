package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult

data class MyState(
    val isScanning: Boolean = false,
    val scanResults: List<ScanResult> = listOf(),
    val deviceToConnectBluetoothGatt: BluetoothGatt? = null,
    val selectedDevice: ScanResult? = null
)
