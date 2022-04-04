package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult

sealed interface WelcomeEvent {
    object RemoveScannedDevices : WelcomeEvent
    object StartScanning : WelcomeEvent
    object StopScanning : WelcomeEvent
    data class StartConnectingToDevice(val scanResult: ScanResult) : WelcomeEvent
    data class EndConnectingToDevice(val gatt: BluetoothGatt) : WelcomeEvent
    data class SetScanningTo(val scanningTo: Boolean) : WelcomeEvent
    data class AddScannedDevice(val scanResult: ScanResult) : WelcomeEvent
    data class SetConnectedToDeviceGatt(val bluetoothGatt: BluetoothGatt?) : WelcomeEvent
    data class SetConnectedTo(val address: String) : WelcomeEvent
}
