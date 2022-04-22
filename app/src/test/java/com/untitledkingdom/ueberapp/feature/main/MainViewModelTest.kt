package com.untitledkingdom.ueberapp.feature.main

import com.juul.kable.Advertisement
import com.tomcz.ellipse.test.processorTest
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.feature.main.state.MainState
import com.untitledkingdom.ueberapp.scanner.ScanService
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

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
    private val advertisement = mockk<Advertisement>()

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
            coEvery { repository.getDataFromDataBaseAsFlow(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(
                    listOf()
                )
            )
        },
        thenStates = {
            assertLast(
                MainState(isPreparing = false, advertisement = advertisement)
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
            coEvery { repository.getDataFromDataBaseAsFlow(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(
                    listOf()
                )
            )
        },
        whenEvent = MainEvent.StartScanning,
        thenStates = {
            assertLast(
                MainState(
                    advertisement = advertisement,
                    values = listOf(),
                    isPreparing = false
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
            coEvery { repository.getDataFromDataBaseAsFlow(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(
                    listOf()
                )
            )
        },
        thenStates = {
            assertLast(
                MainState(
                    advertisement = advertisement,
                    values = listOf(),
                    isPreparing = false
                )
            )
        }
    )

    @Test
    fun `start observing data from repository with error`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.refreshDeviceInfo(any()) } returns flowOf(
                ScanStatus.Found(
                    advertisement
                )
            )
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { repository.getDataFromDataBaseAsFlow(any()) } returns flowOf(
                RepositoryStatus.Error
            )
        },
        thenEffects = {
            assertLast(MainEffect.ShowError("Error during collecting data from DB"))
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
            coEvery { repository.getDataFromDataBaseAsFlow(any()) } returns flowOf(
                RepositoryStatus.SuccessBleData(
                    listOf()
                )
            )
            coEvery { kableService.stopScan() } returns Unit
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { dataStorage.saveToStorage(any(), any()) } returns Unit
            coEvery { repository.clear() } returns Unit
        },
        whenEvent = MainEvent.EndConnectingToDevice,
        thenEffects = {
            assertLast(MainEffect.GoToWelcome)
        }
    )
}
