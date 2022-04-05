package com.untitledkingdom.ueberapp.feature.welcome

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.feature.MyViewModel
import com.untitledkingdom.ueberapp.feature.state.MyEffect
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.welcome.data.BleService
import com.untitledkingdom.ueberapp.utils.RequestCodes
import com.untitledkingdom.ueberapp.utils.printGattTable
import com.untitledkingdom.ueberapp.utils.requestPermission
import com.untitledkingdom.ueberapp.utils.toHexString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@FlowPreview
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    companion object {
        const val SCAN_PERIOD: Long = 5000
    }

    private val myViewModel: MyViewModel by viewModels()
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(
                    permissionType = Manifest.permission.BLUETOOTH_CONNECT,
                    requestCode = RequestCodes.ACCESS_BLUETOOTH_CONNECT,
                    activity = requireActivity(),
                    context = requireContext()
                )
            }
            myViewModel.processor.sendEvent(
                MyEvent.AddScannedDevice(
                    scanResult = result
                )
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(
                    permissionType = Manifest.permission.BLUETOOTH_SCAN,
                    requestCode = RequestCodes.ACCESS_BLUETOOTH_CONNECT,
                    activity = requireActivity(),
                    context = requireContext()
                )
            }
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Timber.d("Connected to device $deviceAddress")
                    myViewModel.processor.sendEvent(
                        MyEvent.SetConnectedToDeviceGatt(
                            bluetoothGatt = gatt
                        )
                    )
                    myViewModel.processor.sendEvent(MyEvent.SetConnectedTo(address = gatt.device.address))
                    gatt.requestMtu(517)
                    gatt.discoverServices()
                }
            } else {
                Timber.d("Error $status encountered for $deviceAddress! Disconnecting...")
                myViewModel.processor.sendEvent(MyEvent.SetConnectedToDeviceGatt(bluetoothGatt = null))
                myViewModel.processor.sendEvent(MyEvent.SetConnectedTo(address = ""))
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(
                    permissionType = Manifest.permission.BLUETOOTH_SCAN,
                    requestCode = RequestCodes.ACCESS_BLUETOOTH_CONNECT,
                    activity = requireActivity(),
                    context = requireContext()
                )
            }
            gatt?.printGattTable()
            gatt?.services?.forEach { bluetoothGattService ->
                val bleService = BleService(
                    serviceUUID = bluetoothGattService.uuid,
                    listAvailableCharacteristics = bluetoothGattService.characteristics
                )
                Timber.d("bluetoothGattService.characteristics ${bluetoothGattService.characteristics.forEach { it.uuid }}")
                readFromCharacteristic(bleService = bleService, gatt)
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

    @SuppressLint("MissingPermission")
    private fun readFromCharacteristic(bleService: BleService, gatt: BluetoothGatt) {
        bleService.listAvailableCharacteristics.forEach { characteristic ->
            val read =
                gatt.getService(bleService.serviceUUID).getCharacteristic(characteristic.uuid)
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(
                    permissionType = Manifest.permission.BLUETOOTH_SCAN,
                    requestCode = RequestCodes.ACCESS_BLUETOOTH_CONNECT,
                    activity = requireActivity(),
                    context = requireContext()
                )
            }
            gatt.readCharacteristic(read)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onProcessor(
            lifecycleState = Lifecycle.State.RESUMED,
            processor = myViewModel::processor,
            onEffect = ::trigger,
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                WelcomeScreen(myViewModel.processor)
            }
        }
    }

    private fun trigger(effect: MyEffect) {
        when (effect) {
            MyEffect.ScanDevices -> scanDevices()
            MyEffect.StopScanDevices -> stopScan()
            is MyEffect.ConnectToDevice -> connectToDevice(effect.scanResult)
            is MyEffect.DisconnectFromDevice -> disconnectFromDevice(effect.gatt)
        }
    }

    private fun disconnectFromDevice(gatt: BluetoothGatt) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                permissionType = Manifest.permission.BLUETOOTH_SCAN,
                requestCode = RequestCodes.ACCESS_BLUETOOTH_SCAN,
                activity = requireActivity(),
                context = requireContext()
            )
        }
        Timber.d("Disconnecting from disconnectFromDevice(gatt)")
        Timber.d("Disconnected from device ${gatt.device.address}")
        myViewModel.processor.sendEvent(
            MyEvent.SetConnectedToDeviceGatt(
                bluetoothGatt = null
            )
        )
        myViewModel.processor.sendEvent(MyEvent.SetConnectedTo(address = ""))
        gatt.close()
        scanDevices()
    }

    private fun connectToDevice(scanResult: ScanResult) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                permissionType = Manifest.permission.BLUETOOTH_SCAN,
                requestCode = RequestCodes.ACCESS_BLUETOOTH_SCAN,
                activity = requireActivity(),
                context = requireContext()
            )
        }
        scanResult.device.connectGatt(requireContext(), false, gattCallback)
    }

    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                permissionType = Manifest.permission.BLUETOOTH_SCAN,
                requestCode = RequestCodes.ACCESS_BLUETOOTH_SCAN,
                activity = requireActivity(),
                context = requireContext()
            )
        }
        myViewModel.processor.sendEvent(MyEvent.SetScanningTo(scanningTo = false))
        bleScanner.stopScan(scanCallback)
    }

    private fun scanDevices() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                permissionType = Manifest.permission.BLUETOOTH_SCAN,
                requestCode = RequestCodes.ACCESS_BLUETOOTH_SCAN,
                activity = requireActivity(),
                context = requireContext()
            )
        }
        myViewModel.processor.sendEvent(MyEvent.SetScanningTo(scanningTo = true))
        bleScanner.startScan(null, scanSettings, scanCallback)
        Handler(Looper.getMainLooper()).postDelayed({
            stopScan()
        }, SCAN_PERIOD)
    }
}
