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
import com.untitledkingdom.ueberapp.utils.printGattTable
import com.untitledkingdom.ueberapp.utils.toHexString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.util.*
import javax.inject.Inject

typealias MyProcessor = Processor<MyEvent, MyState, MyEffect>

@ExperimentalCoroutinesApi
@HiltViewModel
class MyViewModel @Inject constructor(
    private val bleService: BleService,
    private val application: Application
) : ViewModel() {
    val processor: MyProcessor = processor(
        initialState = MyState(),
        onEvent = { event ->
            when (event) {
                MyEvent.StartScanning -> startScanning()
                MyEvent.StopScanning -> stopScanning()
                is MyEvent.SetScanningTo -> flowOf(
                    MyPartialState.SetIsScanning(isScanning = event.scanningTo)
                )
                is MyEvent.AddScannedDevice -> {
                    addScanResult(event.scanResult)
                }
                is MyEvent.StartConnectingToDevice -> connectToDevice(
                    isScanning = state.value.isScanning, scanResult = event.scanResult
                )
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
                is MyEvent.EndConnectingToDevice -> disconnectFromDevice(event.gatt)
                is MyEvent.TabChanged -> flowOf(MyPartialState.TabChanged(event.newTabIndex))
                MyEvent.GoToMainView -> effects.send(MyEffect.GoToMainView).toNoAction()
                is MyEvent.ShowCharacteristics -> {
                    if (state.value.deviceToConnectBluetoothGatt != null)
                        readFromService(
                            serviceUUID = event.uuid,
                            gatt = state.value.deviceToConnectBluetoothGatt!!
                        ).toNoAction()
                    else
                        toNoAction()
                }
            }
        }
    )

    private fun readFromService(serviceUUID: UUID, gatt: BluetoothGatt) {
        if (ActivityCompat.checkSelfPermission(
                application.applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val characteristics = gatt.getService(serviceUUID).characteristics
        characteristics.forEach {
            Timber.d("For Each for ${it.uuid}")
            val characteristicRemote = gatt.getService(serviceUUID).getCharacteristic(it.uuid)
            gatt.readCharacteristic(characteristicRemote)
        }
    }

    private fun startScanning(): Flow<MyPartialState> = flow {
        emit(MyPartialState.RemoveScannedDevices)
        emit(MyPartialState.SetIsScanning(isScanning = true))
        bleService.scan(scanSettings, scanCallback)
    }

    private fun stopScanning(): Flow<MyPartialState> = flow {
        emit(MyPartialState.SetIsScanning(isScanning = false))
        bleService.stopScan(scanCallback)
    }

    private fun connectToDevice(isScanning: Boolean, scanResult: ScanResult): Flow<MyPartialState> =
        flow {
            if (isScanning) {
                emit(MyPartialState.SetIsScanning(isScanning = false))
                bleService.stopScan(scanCallback)
            }
            bleService.connectToDevice(
                scanResult = scanResult,
                gattCallback = gattCallback,
                autoConnect = false
            )
        }

    private fun disconnectFromDevice(gatt: BluetoothGatt): Flow<MyPartialState> = flow {
        bleService.disconnectFromDevice(gatt = gatt)
        emit(MyPartialState.SetConnectedToBluetoothGatt(bluetoothGatt = null))
        emit(MyPartialState.SetConnectedToScanResult(scanResult = null))
    }

    private fun addScanResult(result: ScanResult): Flow<MyPartialState> = flow {
        Timber.d("Result device ${result.device}")
        val scanResults = processor.state.value.scanResults
        val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
        if (indexQuery != -1) {
            val oldScanResult = scanResults[indexQuery]
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
                    processor.sendEvent(MyEvent.GoToMainView)
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
                        Timber.d(
                            "Characteristic string " +
                                characteristic.getStringValue(0)
                        )
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
