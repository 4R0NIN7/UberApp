package com.untitledkingdom.ueberapp.feature.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.utils.functions.toastMessage
import com.untitledkingdom.ueberapp.workManager.ReadingWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.util.concurrent.TimeUnit

@ExperimentalPagerApi
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onProcessor(
            lifecycleState = Lifecycle.State.RESUMED,
            processor = viewModel::processor,
            onEffect = ::trigger,
            viewEvents = ::viewEvents
        )
        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(ReadingWorker::class.java, 15, TimeUnit.MINUTES)
            .addTag("WorkManager")
            .build()
        WorkManager.getInstance(requireContext()).enqueue(
            periodicWorkRequest
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                MainScreenCompose(viewModel.processor)
            }
        }
    }

    private fun viewEvents() = listOf(
        flowOf(MainEvent.ReadCharacteristic),
        flowOf(MainEvent.RefreshDeviceData)
    )

    private fun trigger(effect: MainEffect) {
        when (effect) {
            MainEffect.GoToWelcome -> goToWelcome()
            is MainEffect.ShowData -> toastMessage(
                message = effect.data,
                context = requireContext()
            )
            is MainEffect.ShowError -> toastMessage(
                message = effect.message,
                context = requireContext()
            )
            MainEffect.OpenDetailsForDay -> openDetails()
        }
    }

    private fun openDetails() {
        findNavController().navigate(R.id.detailsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("WorkManager cancelled")
        WorkManager.getInstance(requireContext()).cancelAllWork()
    }

    private fun goToWelcome() {
        toastMessage(message = "Successfully disconnected from device", requireContext())
        findNavController().navigate(R.id.action_mainFragment_to_welcomeFragment)
    }
}
