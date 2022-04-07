package com.untitledkingdom.ueberapp.utils

import android.content.Context
import android.widget.Toast
import timber.log.Timber

fun showError(message: String, context: Context) {
    Toast.makeText(
        context,
        message, Toast.LENGTH_SHORT
    ).show()
    Timber.d(message = message)
}
