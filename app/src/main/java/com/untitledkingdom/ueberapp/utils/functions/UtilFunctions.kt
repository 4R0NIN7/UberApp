package com.untitledkingdom.ueberapp.utils.functions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.service.BackgroundReading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.internal.and
import timber.log.Timber
import java.text.DecimalFormat
import java.time.LocalDateTime

fun requestPermission(
    permissionType: String,
    requestCode: Int,
    activity: Activity,
    context: Context
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            permissionType
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        showAlertDialog(
            context = context,
            permissionType = permissionType,
            requestCode = requestCode,
            activity = activity
        )
    }
}

private fun showAlertDialog(
    context: Context,
    permissionType: String,
    requestCode: Int,
    activity: Activity
) {
    AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.ble_permission_need))
        .setMessage(permissionType)
        .setPositiveButton(
            "OK"
        ) { _, _ ->
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permissionType),
                requestCode
            )
        }
        .create()
        .show()
}

fun toastMessage(message: String, context: Context) {
    Toast.makeText(
        context,
        message, Toast.LENGTH_SHORT
    ).show()
    Timber.d(message = message)
}

val decimalFormat = DecimalFormat("#.##")

fun toDateString(byteArray: ByteArray): String {
    val day = byteArray[0].toUByte()
    val month = byteArray[1].toUByte()
    val year = uBytesToYear(byteArray[3], byteArray[2])
    return "$day$month$year"
}

fun uBytesToYear(high: Byte, low: Byte): Int {
    return high and 0xff shl 8 or (low and 0xff)
}

fun checkIfDateIsTheSame(dateFromDevice: String, date: LocalDateTime): Boolean {
    val dateFromLocalDateTime = "${date.dayOfMonth}${date.monthValue}${date.year}"
    Timber.d("DateFromDevice $dateFromDevice, dateLocal $dateFromLocalDateTime")
    return dateFromDevice == dateFromLocalDateTime
}

@ExperimentalUnsignedTypes
@FlowPreview
@ExperimentalCoroutinesApi
fun controlOverService(actionStartOrResumeService: String, context: Context) =
    Intent(context, BackgroundReading::class.java).also {
        it.action = actionStartOrResumeService
        context.startService(it)
    }

@ExperimentalCoroutinesApi
@FlowPreview
fun observeDevice(device: Device): Flow<Unit> = flow {
    device.deviceStatus()
}
