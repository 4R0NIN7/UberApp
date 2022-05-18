package com.untitledkingdom.ueberapp

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import com.untitledkingdom.ueberapp.service.ReadingService
import com.untitledkingdom.ueberapp.utils.functions.controlOverService
import com.untitledkingdom.ueberapp.utils.functions.requestPermission
import com.untitledkingdom.ueberapp.utils.functions.toastMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@ExperimentalUnsignedTypes
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val locationManager: LocationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }
    private val serviceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ReadingService.INTENT_MESSAGE_FROM_SERVICE) {
                navigateToMainFragment(Intent(ReadingService.ACTION_SHOW_MAIN_FRAGMENT))
            }
        }
    }
    private val locationBroadcastReceiver = LocationBroadcastReceiver()
    private val bluetoothBroadcastReceiver = BluetoothBroadcastReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateToMainFragment(intent)
        if (intent.extras != null) {
            if (intent.extras?.getBoolean(ActivityConst.ENABLE_BLUETOOTH) == true) {
                enableBluetooth()
            } else if (intent.extras?.getBoolean(ActivityConst.ENABLE_GPS) == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    enableLocation()
                }
            }
        }
        checkPermission()
        val bluetoothFilter = IntentFilter()
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        val gpsFilter = IntentFilter()
        gpsFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(bluetoothBroadcastReceiver, bluetoothFilter)
        registerReceiver(locationBroadcastReceiver, gpsFilter)
        checkBatteryOptimizations()
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        Timber.d(
            "powerManager.isIgnoringBatteryOptimizations(name) ${
            powerManager.isIgnoringBatteryOptimizations(
                name
            )
            }"
        )
        return powerManager.isIgnoringBatteryOptimizations(name)
    }

    private fun checkBatteryOptimizations() {
        if (!isIgnoringBatteryOptimizations(this)) {
            val name = resources.getString(R.string.app_name)
            toastMessage(
                "Battery optimization -> All apps -> $name -> Don't optimize",
                this
            )
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
    }

    private fun checkPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            )
        }
        permissions.forEach {
            requestPermission(
                permissionType = it,
                requestCode = ActivityConst.PERMISSION_CODE,
                activity = this,
                context = this
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothBroadcastReceiver)
        unregisterReceiver(locationBroadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                serviceReceiver,
                IntentFilter(ReadingService.INTENT_MESSAGE_FROM_SERVICE)
            )
        if (isIgnoringBatteryOptimizations(this)) {
            Timber.d("IsIgnoringBatteryOptimizations Starting service in activity")
            controlOverService(ReadingService.ACTION_START_OR_RESUME_SERVICE, this)
        }
        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        } else if (!locationManager.isLocationEnabled) {
            enableLocation()
        }
    }

    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBluetoothIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun enableLocation() {
        if (!locationManager.isLocationEnabled) {
            val enableLocation = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            requestGps.launch(enableLocation)
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                restart()
            }
        }

    private var requestGps =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                restart()
            }
        }

    private fun restart() {
        val packageManager: PackageManager = this.packageManager
        val intent = packageManager.getLaunchIntentForPackage(this.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        this.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToMainFragment(intent)
    }

    private fun navigateToMainFragment(intent: Intent?) {
        if (intent?.action == ReadingService.ACTION_SHOW_MAIN_FRAGMENT) {
            navController.navigate(R.id.action_global_mainFragment)
        }
    }
}

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class BluetoothBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> {
                    val newIntent = Intent(context, MainActivity::class.java)
                    newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    newIntent.putExtra(ActivityConst.ENABLE_BLUETOOTH, true)
                    if (ActivityCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestPermission(
                                permissionType = Manifest.permission.BLUETOOTH_CONNECT,
                                requestCode = ActivityConst.PERMISSION_CODE,
                                activity = context.applicationContext as Activity,
                                context = context
                            )
                        }
                    }
                    context.startActivity(newIntent)
                }
                else -> {}
            }
        }
    }
}

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class LocationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager =
                context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGpsEnabled) {
                val newIntent = Intent(context, MainActivity::class.java)
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                newIntent.putExtra(ActivityConst.ENABLE_GPS, true)
                context.startActivity(newIntent)
            }
        }
    }
}
