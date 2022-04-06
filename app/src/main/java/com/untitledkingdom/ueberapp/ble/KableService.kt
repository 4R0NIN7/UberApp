package com.untitledkingdom.ueberapp.ble

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.untitledkingdom.ueberapp.ble.data.ScanStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface KableService {
    fun scan(): Flow<ScanStatus>
    fun stopScan()
    fun returnPeripheral(scope: CoroutineScope, advertisement: Advertisement): Peripheral
}
