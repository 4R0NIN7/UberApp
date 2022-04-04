package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.le.ScanResult
import com.tomcz.ellipse.PartialState

sealed interface WelcomePartialState : PartialState<WelcomeState> {
    data class SetScanningId(val scanningTo: Boolean) : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(isScanning = scanningTo)
    }

    data class AddScanResult(val scannedDevice: ScanResult) : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(scannedDevices = oldState.scannedDevices + scannedDevice)
    }
}
