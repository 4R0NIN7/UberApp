package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import com.juul.kable.Advertisement

sealed interface MyEvent {
    object RemoveScannedDevices : MyEvent
    object StartScanning : MyEvent
    object StopScanning : MyEvent
    data class TabChanged(val newTabIndex: Int) : MyEvent
    data class StartConnectingToDevice(val advertisement: Advertisement) : MyEvent
    data class EndConnectingToDevice(val gatt: BluetoothGatt) : MyEvent
    data class SetScanningTo(val scanningTo: Boolean) : MyEvent
    data class SetConnectedToDeviceGatt(val bluetoothGatt: BluetoothGatt?) : MyEvent
    data class SetConnectedTo(val address: String) : MyEvent
}
