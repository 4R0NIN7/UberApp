package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import com.juul.kable.Advertisement
import com.tomcz.ellipse.PartialState

sealed interface MyPartialState : PartialState<MyState> {
    object RemoveAdvertisements : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisements = listOf())
    }

    data class SetIsScanning(val isScanning: Boolean) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(isScanning = isScanning)
    }

    data class SetAdvertisements(val newAdvertisement: List<Advertisement>) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisements = newAdvertisement)
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
