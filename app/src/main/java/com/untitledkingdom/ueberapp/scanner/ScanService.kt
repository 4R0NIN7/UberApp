package com.untitledkingdom.ueberapp.scanner

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ScanService {
    fun scan(): Flow<ScanStatus>
    fun refreshDeviceInfo(macAddress: String): Flow<ScanStatus>
    fun stopScan()
    fun returnPeripheral(scope: CoroutineScope, advertisement: Advertisement): Peripheral
}
