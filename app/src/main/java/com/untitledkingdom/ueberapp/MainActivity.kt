package com.untitledkingdom.ueberapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.untitledkingdom.ueberapp.utils.RequestCodes
import com.untitledkingdom.ueberapp.utils.requestPermission
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission(
            permissionType = Manifest.permission.BLUETOOTH_SCAN,
            requestCode = RequestCodes.ACCESS_BLUETOOTH_SCAN,
            activity = this,
            context = this
        )
        requestPermission(
            permissionType = Manifest.permission.BLUETOOTH_CONNECT,
            requestCode = RequestCodes.ACCESS_BLUETOOTH_CONNECT,
            activity = this,
            context = this
        )
        requestPermission(
            permissionType = Manifest.permission.ACCESS_FINE_LOCATION,
            requestCode = RequestCodes.ACCESS_FINE_LOCATION,
            activity = this,
            context = this
        )
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBluetoothIntent)
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Timber.d("Bluetooth enabled")
            } else {
                Timber.d("Bluetooth disabled")
            }
        }
}
