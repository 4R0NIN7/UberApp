package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import com.tomcz.ellipse.PartialState

sealed interface MyPartialState : PartialState<MyState> {
    object RemoveScannedDevices : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(scanResults = listOf())
    }

    data class SetIsScanning(val isScanning: Boolean) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(isScanning = isScanning)
    }

    data class AddScanResult(val scanResult: ScanResult) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(scanResults = oldState.scanResults + scanResult)
    }

    data class RemoveScanResult(val scanResult: ScanResult) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(scanResults = oldState.scanResults - scanResult)
    }

    data class SetConnectedToBluetoothGatt(val bluetoothGatt: BluetoothGatt?) :
        MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(deviceToConnectBluetoothGatt = bluetoothGatt)
    }

    data class SetConnectedToScanResult(val scanResult: ScanResult?) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(selectedDevice = scanResult)
    }

    data class TabChanged(val newTabIndex: Int) : MyPartialState {
        override fun reduce(oldState: MyState): MyState = oldState.copy(tabIndex = newTabIndex)
    }
}
