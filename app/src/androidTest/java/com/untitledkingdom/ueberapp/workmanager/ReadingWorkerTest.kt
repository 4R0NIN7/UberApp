package com.untitledkingdom.ueberapp.workmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.service.ReadingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

@ObsoleteCoroutinesApi
@ExperimentalUnsignedTypes
@FlowPreview
@ExperimentalCoroutinesApi
class ReadingWorkerTest {
    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = StandardTestDispatcher()
    private lateinit var context: Context
    private val repository: ReadingRepository = mockk()
    private val device: Device = mockk()
    private val reading: Reading = mockk()
    private val foregroundInfo = mockk<ForegroundInfo>()
    private val worker = mockk<ReadingWorker>()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testReadingWorker() = runTest {
        coEvery { repository.start(any()) } returns Unit
        coEvery { repository.saveData(any(), reading = reading) } returns Unit
        coEvery { repository.stop() } returns Unit
        coEvery { device.observationOnDataCharacteristic() } returns flowOf(reading)
        coEvery { worker.doWork() } answers { callOriginal() }
        coEvery { worker.setForeground(any()) } returns Unit
        every { worker.device } returns device
        every { worker.repository } returns repository
        every { worker["getScope"]() } returns scope
        every { worker.stop() } returns Unit
        every { worker["createForegroundInfo"]() } returns foregroundInfo
        every { worker.isStopped } returns false
        /*-------------Test prepare----------------*/
        val result = worker.doWork()
        worker.stop()
        /*-------------Verification----------------*/
        coVerify {
            worker.doWork()
            worker.stop()
        }
        assertTrue(result !is ListenableWorker.Result.Failure)
        assertTrue(result !is ListenableWorker.Result.Retry)
        assertTrue(result is ListenableWorker.Result.Success)
    }
}
