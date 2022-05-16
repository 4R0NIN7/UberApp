package com.untitledkingdom.ueberapp.feature.main

import com.juul.kable.Advertisement
import com.tomcz.ellipse.test.processorTest
import com.untitledkingdom.ueberapp.database.data.BleDataCharacteristics
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.feature.main.state.MainState
import com.untitledkingdom.ueberapp.scanner.ScanService
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import com.untitledkingdom.ueberapp.utils.functions.toDeviceReading
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalUnsignedTypes
@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModelTest : BaseCoroutineTest() {
    private val dataStorage by lazy { mockk<DataStorage>() }
    private val kableService by lazy { mockk<ScanService>() }
    private val repository by lazy { mockk<MainRepository>() }
    private val viewModel: MainViewModel by lazy {
        MainViewModel(
            repository,
            dataStorage,
            kableService
        )
    }
    private val localDateTime: LocalDateTime = LocalDateTime.of(
        1970,
        1,
        1,
        1,
        1,
        1
    )
    private val advertisement = mockk<Advertisement>()
    private val bleDataEntity = BleDataEntity(
        id = 100,
        temperature = 10f,
        humidity = 52,
        dateTime = localDateTime,
        serviceUUID = "11223344"
    )
    private val bleDataCharacteristics = mockk<BleDataCharacteristics>()

    @Test
    fun `initial state`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.scan() } returns flowOf()
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { kableService.refreshDeviceInfo(any()) } returns flowOf(
                ScanStatus.Found(
                    advertisement
                )
            )
            coEvery { repository.getCharacteristicsPerDay() } returns flowOf(
                RepositoryStatus.SuccessBleCharacteristics(listOf(bleDataCharacteristics))
            )
            coEvery { repository.getLastDataFromDataBase(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(null)
            )
        },
        thenStates = {
            assertLast(
                MainState(
                    advertisement = advertisement,
                    dataCharacteristics = listOf(bleDataCharacteristics)
                )
            )
        }
    )

    @Test
    fun `start scanning`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.refreshDeviceInfo(any()) } returns flowOf(
                ScanStatus.Found(
                    advertisement
                )
            )
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { repository.getLastDataFromDataBase(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(
                    null
                )
            )
            coEvery { repository.getCharacteristicsPerDay() } returns flowOf(
                RepositoryStatus.SuccessBleCharacteristics(
                    listOf(bleDataCharacteristics)
                )
            )
        },
        whenEvent = MainEvent.StartScanning,
        thenStates = {
            assertLast(
                MainState(
                    advertisement = advertisement,
                    values = listOf(),
                    dataCharacteristics = listOf(bleDataCharacteristics)
                )
            )
        }
    )

    @Test
    fun `start observing data from repository`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.refreshDeviceInfo(any()) } returns flowOf(
                ScanStatus.Found(
                    advertisement
                )
            )
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { repository.getDataFilteredByDate(any(), any()) } returns flowOf(
                RepositoryStatus.SuccessGetListBleData(
                    listOf()
                )
            )
            coEvery { repository.getLastDataFromDataBase(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(bleDataEntity.toDeviceReading())
            )
            coEvery { repository.getLastDataFromDataBase(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(bleDataEntity.toDeviceReading())
            )
            coEvery { repository.getCharacteristicsPerDay() } returns flowOf(
                RepositoryStatus.SuccessBleCharacteristics(listOf(bleDataCharacteristics))
            )
        },
        thenStates = {
            assertLast(
                MainState(
                    advertisement = advertisement,
                    values = listOf(),
                    lastDeviceReading = bleDataEntity.toDeviceReading(),
                    dataCharacteristics = listOf(bleDataCharacteristics)
                )
            )
        }
    )

    @Test
    fun `disconnect from device`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.refreshDeviceInfo(any()) } returns flowOf(
                ScanStatus.Found(
                    advertisement
                )
            )
            coEvery { repository.getLastDataFromDataBase(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(
                    bleDataEntity.toDeviceReading()
                )
            )
            coEvery { repository.getCharacteristicsPerDay() } returns flowOf(
                RepositoryStatus.SuccessBleCharacteristics(
                    listOf(bleDataCharacteristics)
                )
            )
            coEvery { kableService.stopScan() } returns Unit
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { dataStorage.saveToStorage(any(), any()) } returns Unit
        },
        whenEvent = MainEvent.EndConnectingToDevice,
        thenEffects = {
            assertLast(MainEffect.GoToWelcome)
        }
    )
}
