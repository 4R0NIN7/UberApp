package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult

data class WelcomeState(
    val isScanning: Boolean = false,
    val scanResults: List<ScanResult> = listOf(),
    val selectedDeviceToConnect: BluetoothGatt? = null
)
