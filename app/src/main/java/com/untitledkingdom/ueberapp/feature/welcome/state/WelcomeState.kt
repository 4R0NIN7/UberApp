package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.le.ScanResult

data class WelcomeState(
    val isScanning: Boolean = false,
    val scannedDevices: Set<ScanResult> = setOf()
)
