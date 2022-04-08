package com.untitledkingdom.ueberapp.utils.functions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.untitledkingdom.ueberapp.R
import timber.log.Timber
import kotlin.random.Random

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

fun generateRandomString(): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..15)
        .map { i -> Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}
