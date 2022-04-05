package com.untitledkingdom.ueberapp.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings

interface BleService {
    fun scan(scanSettings: ScanSettings, scanCallback: ScanCallback)
    fun stopScan(scanCallback: ScanCallback)
    fun connectToDevice(
        scanResult: ScanResult,
        gattCallback: BluetoothGattCallback,
        autoConnect: Boolean
    )
    fun disconnectFromDevice(gatt: BluetoothGatt)
}
