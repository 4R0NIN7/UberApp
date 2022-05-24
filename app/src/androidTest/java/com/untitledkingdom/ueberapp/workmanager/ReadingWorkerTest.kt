package com.untitledkingdom.ueberapp.workmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.TestListenableWorkerBuilder
import com.untitledkingdom.ueberapp.background.workmanager.ReadingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
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
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
    private val delay = 2000L

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
        val worker = TestListenableWorkerBuilder<ReadingWorker>(context).build()
        val job = scope.launch {
            worker.doWork()
        }
        delay(delay)
        job.cancel()
        worker.stop()
        assertFalse(job.isActive)
        assertTrue(worker.isStopped)
    }
}
