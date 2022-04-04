package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult

sealed interface WelcomeEffect {
    object ScanDevices : WelcomeEffect
    object StopScanDevices : WelcomeEffect
    data class ConnectToDevice(val scanResult: ScanResult) : WelcomeEffect
    data class DisconnectFromDevice(val gatt: BluetoothGatt) : WelcomeEffect
}
