package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import com.juul.kable.Advertisement

data class MyState(
    val tabIndex: Int = 0,
    val isScanning: Boolean = false,
    val advertisements: List<Advertisement> = listOf(),
    val deviceToConnectBluetoothGatt: BluetoothGatt? = null,
    val selectedDevice: ScanResult? = null,
)
