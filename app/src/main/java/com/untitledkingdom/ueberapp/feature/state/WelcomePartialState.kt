package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import com.tomcz.ellipse.PartialState

sealed interface WelcomePartialState : PartialState<MyState> {
    object RemoveScannedDevices : WelcomePartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(scanResults = listOf())
    }

    data class SetScanningId(val scanningTo: Boolean) : WelcomePartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(isScanning = scanningTo)
    }

    data class AddScanResult(val scanResult: ScanResult) : WelcomePartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(scanResults = oldState.scanResults + scanResult)
    }

    data class RemoveScanResult(val scanResult: ScanResult) : WelcomePartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(scanResults = oldState.scanResults - scanResult)
    }

    data class SetConnectedToBluetoothGatt(val bluetoothGatt: BluetoothGatt?) :
        WelcomePartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(deviceToConnectBluetoothGatt = bluetoothGatt)
    }

    data class SetConnectedToScanResult(val scanResult: ScanResult?) : WelcomePartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(selectedDevice = scanResult)
    }
}
