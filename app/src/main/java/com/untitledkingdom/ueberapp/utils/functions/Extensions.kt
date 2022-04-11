package com.untitledkingdom.ueberapp.utils.functions

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)

fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

fun Advertisement.toScannedDevice() = ScannedDevice(
    address = address,
    name = name ?: "Unknown name",
    rssi = rssi
)

fun Dp.toPx(density: Density) = value * density.density
