package com.untitledkingdom.ueberapp.feature.welcome

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
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
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.utils.RequestCodes
import com.untitledkingdom.ueberapp.utils.printGattTable
import com.untitledkingdom.ueberapp.utils.requestPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@FlowPreview
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    companion object {
        const val SCAN_PERIOD: Long = 10000
    }

    private val viewModel: WelcomeViewModel by viewModels()
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
            viewModel.processor.sendEvent(
                WelcomeEvent.AddScannedDevice(
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
                    viewModel.processor.sendEvent(WelcomeEvent.SetConnectedTo(device = gatt))
                    gatt.discoverServices()
                    gatt.printGattTable()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Timber.d("Disconnected from device $deviceAddress")
                    viewModel.processor.sendEvent(WelcomeEvent.SetConnectedTo(device = null))
                    gatt.close()
                    stopScan()
                }
            } else {
                Timber.d("Error $status encountered for $deviceAddress! Disconnecting...")
                viewModel.processor.sendEvent(WelcomeEvent.SetConnectedTo(device = null))
                gatt.close()
                stopScan()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onProcessor(
            lifecycleState = Lifecycle.State.RESUMED,
            processor = viewModel::processor,
            onEffect = ::trigger,
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                WelcomeScreen(viewModel.processor)
            }
        }
    }

    private fun trigger(effect: WelcomeEffect) {
        when (effect) {
            WelcomeEffect.ScanDevices -> scanDevices()
            WelcomeEffect.StopScanDevices -> stopScan()
            is WelcomeEffect.ConnectToDevice -> connectToDevice(effect.scanResult)
        }
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
        viewModel.processor.sendEvent(WelcomeEvent.SetScanningTo(scanningTo = false))
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
        viewModel.processor.sendEvent(WelcomeEvent.SetScanningTo(scanningTo = true))
        bleScanner.startScan(null, scanSettings, scanCallback)
        Handler(Looper.getMainLooper()).postDelayed({
            stopScan()
        }, SCAN_PERIOD)
    }
}
