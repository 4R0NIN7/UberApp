package com.untitledkingdom.ueberapp.utils.functions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.untitledkingdom.ueberapp.R
import timber.log.Timber
import java.text.DecimalFormat
import java.time.LocalDateTime

object RequestCodes {
    const val PERMISSION_CODE = 1337
}

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

private fun uBytesToYear(high: Byte, low: Byte): Int {
    return (0xFF and high.toInt()) * 256 + (0xFF and low.toInt())
}

fun checkIfDateIsTheSame(dateFromDevice: String, date: LocalDateTime): Boolean {
    val dateFromLocalDateTime = "${date.dayOfMonth}${date.monthValue}${date.year}"
    Timber.d("DateFromDevice $dateFromDevice, dateLocal $dateFromLocalDateTime")
    return dateFromDevice == dateFromLocalDateTime
}
