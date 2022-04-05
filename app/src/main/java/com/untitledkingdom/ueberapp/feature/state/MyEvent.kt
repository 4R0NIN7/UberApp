package com.untitledkingdom.ueberapp.feature.state

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import java.util.*

sealed interface MyEvent {
    object RemoveScannedDevices : MyEvent
    object StartScanning : MyEvent
    object StopScanning : MyEvent
    object GoToMainView : MyEvent
    data class ShowCharacteristics(val uuid: UUID) : MyEvent
    data class TabChanged(val newTabIndex: Int) : MyEvent
    data class StartConnectingToDevice(val scanResult: ScanResult) : MyEvent
    data class EndConnectingToDevice(val gatt: BluetoothGatt) : MyEvent
    data class SetScanningTo(val scanningTo: Boolean) : MyEvent
    data class AddScannedDevice(val scanResult: ScanResult) : MyEvent
    data class SetConnectedToDeviceGatt(val bluetoothGatt: BluetoothGatt?) : MyEvent
    data class SetConnectedTo(val address: String) : MyEvent
}
