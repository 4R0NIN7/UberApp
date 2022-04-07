package com.untitledkingdom.ueberapp.utils

import android.content.Context
import android.widget.Toast
import timber.log.Timber
import kotlin.random.Random

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
