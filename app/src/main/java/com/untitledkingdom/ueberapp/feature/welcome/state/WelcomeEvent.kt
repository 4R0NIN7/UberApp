package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.le.ScanResult

sealed interface WelcomeEvent {
    object StartScanning : WelcomeEvent
    object StopScanning : WelcomeEvent
    data class StartConnectingToDevice(val scanResult: ScanResult) : WelcomeEvent
    data class SetScanningTo(val scanningTo: Boolean) : WelcomeEvent
    data class AddScannedDevice(val scanResult: ScanResult) : WelcomeEvent
}
