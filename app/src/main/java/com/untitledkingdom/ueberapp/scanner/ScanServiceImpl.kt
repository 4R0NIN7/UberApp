package com.untitledkingdom.ueberapp.scanner

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import javax.inject.Inject

class ScanServiceImpl @Inject constructor() : ScanService {
    private val scanner = Scanner {
        filters = null
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Data
            format = Logging.Format.Multiline
        }
    }
    private var isScanning = true

    override fun scan(): Flow<ScanStatus> =
        scanner
            .advertisements
            .catch { cause ->
                ScanStatus.Failed(
                    cause.message ?: "Error during scanning!"
                )
            }.map { advertisement ->
                if (!advertisement.name.isNullOrEmpty()) {
                    ScanStatus.Found(advertisement = advertisement)
                } else {
                    ScanStatus.Omit
                }
            }.onStart {
                isScanning = true
                emit(ScanStatus.Scanning)
            }.takeWhile {
                isScanning
            }.onCompletion {
                emit(ScanStatus.Stopped)
            }

    override fun refreshDeviceInfo(macAddress: String): Flow<ScanStatus> =
        scanner
            .advertisements
            .catch { cause ->
                ScanStatus.Failed(
                    cause.message ?: "Error during scanning!"
                )
            }
            .map { advertisement ->
                if (advertisement.address == macAddress) {
                    ScanStatus.Found(advertisement = advertisement)
                } else {
                    ScanStatus.Omit
                }
            }.onStart {
                emit(ScanStatus.Scanning)
                isScanning = true
            }.takeWhile {
                isScanning
            }.onCompletion {
                emit(ScanStatus.Stopped)
            }

    override fun stopScan() {
        isScanning = false
    }

    override fun returnPeripheral(scope: CoroutineScope, advertisement: Advertisement): Peripheral {
        return scope.peripheral(advertisement = advertisement) {
            logging {
                engine = SystemLogEngine
                level = Logging.Level.Data
                format = Logging.Format.Multiline
                data = Logging.DataProcessor {
                    String(it)
                }
            }
        }
    }
}
