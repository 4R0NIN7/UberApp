package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.le.ScanResult
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice

sealed interface WelcomeEvent {
    object StartScanning : WelcomeEvent
    object StopScanning : WelcomeEvent
    data class StartConnectingToDevice(val selectedDevice: ScannedDevice) : WelcomeEvent
    data class SetScanningTo(val scanningTo: Boolean) : WelcomeEvent
    data class AddScannedDevice(val scanResult: ScanResult) : WelcomeEvent
}
