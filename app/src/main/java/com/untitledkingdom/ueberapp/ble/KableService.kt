package com.untitledkingdom.ueberapp.ble

import kotlinx.coroutines.flow.Flow

interface KableService {
    fun startScan(): Flow<ScanStatus>
    fun stopScan()
    fun connect()
    fun disconnect()
}
