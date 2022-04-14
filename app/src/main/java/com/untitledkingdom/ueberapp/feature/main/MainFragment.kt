package com.untitledkingdom.ueberapp.feature.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.service.BackgroundReading
import com.untitledkingdom.ueberapp.utils.functions.controlOverService
import com.untitledkingdom.ueberapp.utils.functions.toastMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalMaterialApi
@ExperimentalUnsignedTypes
@ExperimentalPagerApi
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onProcessor(
            lifecycleState = Lifecycle.State.RESUMED,
            processor = viewModel::processor,
            onEffect = ::trigger,
        )
        controlOverService(BackgroundReading.ACTION_START_OR_RESUME_SERVICE, requireContext())
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                MainScreenCompose(viewModel.processor)
            }
        }
    }

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
        }
    }

    private fun goToWelcome() {
        toastMessage(message = "Successfully disconnected from device", requireContext())
        controlOverService(BackgroundReading.ACTION_STOP_SERVICE, requireContext())
        findNavController().navigate(R.id.action_mainFragment_to_welcomeFragment)
    }
}
