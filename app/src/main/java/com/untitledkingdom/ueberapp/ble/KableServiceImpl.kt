package com.untitledkingdom.ueberapp.ble

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.ble.data.ScanStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject

class KableServiceImpl @Inject constructor() : KableService {
    private val scanner = Scanner {
        filters = null
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Data
            format = Logging.Format.Multiline
        }
    }
    private var isScanning = true
    override fun scan(): Flow<ScanStatus> = flow {
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
                Timber.d("Mac Address ${advertisement.address}")
                if (advertisement.name != null) {
                    emit(
                        ScanStatus.Found(
                            advertisement = advertisement
                        )
                    )
                }
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

    override fun refreshDeviceData(selectedAdvertisement: Advertisement): Flow<ScanStatus> = flow {
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
                if (advertisement.address == selectedAdvertisement.address) {
                    emit(
                        ScanStatus.Found(
                            advertisement = advertisement
                        )
                    )
                }
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

    override fun returnPeripheral(scope: CoroutineScope, advertisement: Advertisement): Peripheral {
        Timber.d("Returning Peripheral")
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
