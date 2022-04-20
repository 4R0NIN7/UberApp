package com.untitledkingdom.ueberapp.feature.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.service.BackgroundService
import com.untitledkingdom.ueberapp.utils.functions.controlOverService
import com.untitledkingdom.ueberapp.utils.functions.toastMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private val welcomeViewModel: WelcomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onProcessor(
            lifecycleState = Lifecycle.State.RESUMED,
            processor = welcomeViewModel::processor,
            onEffect = ::trigger,
            viewEvents = ::events
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                WelcomeScreen(welcomeViewModel.processor)
            }
        }
    }

    private fun events() = listOf(flowOf(WelcomeEvent.StartService))

    private fun trigger(effect: WelcomeEffect) {
        when (effect) {
            WelcomeEffect.GoToMain -> goToMainFragment()
            is WelcomeEffect.ShowError -> toastMessage(
                message = effect.message,
                context = requireContext()
            )
            WelcomeEffect.StartService -> startService()
            else -> {}
        }
    }

    private fun startService() {
        controlOverService(BackgroundService.ACTION_START_OR_RESUME_SERVICE, requireContext())
    }

    private fun goToMainFragment() {
        Toast.makeText(requireContext(), "Successfully connected to device!", Toast.LENGTH_SHORT)
            .show()
        if (!BackgroundService.isRunning) {
            startService()
        }
        findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
    }
}
