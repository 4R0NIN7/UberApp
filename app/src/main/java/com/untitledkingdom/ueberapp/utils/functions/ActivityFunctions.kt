package com.untitledkingdom.ueberapp.utils.functions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.service.ReadingService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

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

@ExperimentalUnsignedTypes
@FlowPreview
@ExperimentalCoroutinesApi
fun controlOverService(actionStartOrResumeService: String, context: Context) =
    Intent(context, ReadingService::class.java).also {
        it.action = actionStartOrResumeService
        context.startService(it)
    }
