package com.untitledkingdom.ueberapp.ble

import com.juul.kable.Scanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject

class KableServiceImpl @Inject constructor() : KableService {
    private val scanner = Scanner()
    private var isScanning = true
    override fun startScan(): Flow<ScanStatus> = flow {
        scanner
            .advertisements
            .catch { cause ->
                emit(
                    ScanStatus.Failed(
                        cause.message ?: "Error during scanning!"
                    )
                )
            }
            .collect { advertisement ->
                emit(
                    ScanStatus.Found(
                        advertisement = advertisement
                    )
                )
            }
    }.onStart {
        emit(ScanStatus.Scanning)
        isScanning = true
    }.takeWhile {
        isScanning
    }.onCompletion {
        emit(ScanStatus.Stopped)
        isScanning = true
    }

    override fun stopScan() {
        Timber.d("Stopping scanning")
        isScanning = false
    }

    override fun connect() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }
}
