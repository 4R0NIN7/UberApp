package com.untitledkingdom.ueberapp.feature.welcome.data

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

data class BleData(
    val serviceUUID: UUID,
    val listAvailableCharacteristics: List<BluetoothGattCharacteristic>
)
