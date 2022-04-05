package com.untitledkingdom.ueberapp.feature

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.ble.BleService
import com.untitledkingdom.ueberapp.feature.state.MyEffect
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.state.MyPartialState
import com.untitledkingdom.ueberapp.feature.state.MyState
import com.untitledkingdom.ueberapp.feature.welcome.data.BleData
import com.untitledkingdom.ueberapp.utils.printGattTable
import com.untitledkingdom.ueberapp.utils.toHexString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject

typealias MyProcessor = Processor<MyEvent, MyState, MyEffect>

@HiltViewModel
class MyViewModel @Inject constructor(
    private val bleService: BleService,
    private val application: Application
) : ViewModel() {
    val processor: MyProcessor = processor(
        initialState = MyState(),
        onEvent = { event ->
            when (event) {
                MyEvent.StartScanning -> {
                    flowOf(MyPartialState.SetIsScanning(true))
                    bleService.scan(scanSettings, scanCallback).toNoAction()
                }
                MyEvent.StopScanning -> {
                    flowOf(MyPartialState.SetIsScanning(false))
                    bleService.stopScan(scanCallback).toNoAction()
                }
                is MyEvent.SetScanningTo -> flowOf(
                    MyPartialState.SetIsScanning(isScanning = event.scanningTo)
                )
                is MyEvent.AddScannedDevice -> {
                    addScanResult(event.scanResult)
                }
                is MyEvent.StartConnectingToDevice -> {
                    if (state.value.isScanning) {
                        effects.send(MyEffect.StopScanDevices)
                    }
                    Timber.d("Device from scanResult ${event.scanResult.device}")
                    bleService.connectToDevice(
                        scanResult = event.scanResult,
                        gattCallback = gattCallback,
                        autoConnect = false
                    ).toNoAction()
                }
                MyEvent.RemoveScannedDevices -> flowOf(MyPartialState.RemoveScannedDevices)
                is MyEvent.SetConnectedToDeviceGatt -> flowOf(
                    MyPartialState.SetConnectedToBluetoothGatt(
                        event.bluetoothGatt
                    )
                )
                is MyEvent.SetConnectedTo -> {
                    if (event.address != "") {
                        val scanResult =
                            state.value.scanResults.first { it.device.address == event.address }
                        flowOf(MyPartialState.SetConnectedToScanResult(scanResult))
                    } else {
                        flowOf(MyPartialState.SetConnectedToScanResult(scanResult = null))
                    }
                }
                is MyEvent.EndConnectingToDevice -> bleService.disconnectFromDevice(gatt = event.gatt)
                    .toNoAction()
                is MyEvent.TabChanged -> flowOf(MyPartialState.TabChanged(event.newTabIndex))
            }
        }
    )

    private fun addScanResult(result: ScanResult): Flow<MyPartialState> = flow {
        Timber.d("Result device ${result.device}")
        val scanResults = processor.state.value.scanResults
        val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
        if (indexQuery != -1) {
            val oldScanResult = scanResults[indexQuery]
            Timber.d("Result already exists in a list $oldScanResult")
            Timber.d("Old ScanResult device ${oldScanResult.device}")
            emit(MyPartialState.RemoveScanResult(oldScanResult))
            emit(MyPartialState.AddScanResult(result))
        } else {
            emit(MyPartialState.AddScanResult(scanResult = result))
        }
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (ActivityCompat.checkSelfPermission(
                    application.applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            processor.sendEvent(
                MyEvent.AddScannedDevice(
                    scanResult = result
                )
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(
                    application.applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Timber.d("Connected to device $deviceAddress")
                    processor.sendEvent(
                        MyEvent.SetConnectedToDeviceGatt(
                            bluetoothGatt = gatt
                        )
                    )
                    processor.sendEvent(MyEvent.SetConnectedTo(address = gatt.device.address))
                    gatt.requestMtu(517)
                    gatt.discoverServices()
                }
            } else {
                Timber.d("Error $status encountered for $deviceAddress! Disconnecting...")
                processor.sendEvent(MyEvent.SetConnectedToDeviceGatt(bluetoothGatt = null))
                processor.sendEvent(MyEvent.SetConnectedTo(address = ""))
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (ActivityCompat.checkSelfPermission(
                    application.applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            gatt?.printGattTable()
            gatt?.services?.forEach { bluetoothGattService ->
                val bleData = BleData(
                    serviceUUID = bluetoothGattService.uuid,
                    listAvailableCharacteristics = bluetoothGattService.characteristics
                )
                Timber.d("bluetoothGattService.characteristics ${bluetoothGattService.characteristics.forEach { it.uuid }}")
                readFromCharacteristic(bleData = bleData, gatt)
            }
        }

        private fun readFromCharacteristic(bleData: BleData, gatt: BluetoothGatt) {
            bleData.listAvailableCharacteristics.forEach { characteristic ->
                val read =
                    gatt.getService(bleData.serviceUUID).getCharacteristic(characteristic.uuid)
                if (ActivityCompat.checkSelfPermission(
                        application.applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt.readCharacteristic(read)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    if (characteristic != null) {
                        Timber.d(
                            "Read characteristic ${characteristic.uuid}:\n" +
                                "${characteristic.value?.toHexString()}"
                        )
                        Timber.d("Characteristic string ${characteristic.getStringValue(0)}")
                    }
                }
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    Timber.d("Read not permitted for ${characteristic?.uuid}!")
                }
                else -> {
                    Timber.d("Characteristic read failed for ${characteristic?.uuid}, error: $status")
                }
            }
        }
    }
}
