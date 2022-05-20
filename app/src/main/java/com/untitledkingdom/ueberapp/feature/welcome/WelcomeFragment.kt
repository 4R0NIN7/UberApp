package com.untitledkingdom.ueberapp.feature.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.background.workmanager.ReadingWorker
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.utils.functions.isWorkScheduled
import com.untitledkingdom.ueberapp.utils.functions.startWorker
import com.untitledkingdom.ueberapp.utils.functions.toastMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

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
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                WelcomeScreen(welcomeViewModel.processor)
            }
        }
    }

    private fun trigger(effect: WelcomeEffect) {
        when (effect) {
            WelcomeEffect.GoToMain -> goToMainFragment()
            is WelcomeEffect.ShowError -> toastMessage(
                message = effect.message,
                context = requireContext()
            )
            else -> {}
        }
    }

    private fun goToMainFragment() {
        toastMessage("Successfully connected to device!", requireContext())
        if (!isWorkScheduled(ReadingWorker.WORK_NAME, requireContext())) {
            startWorker(requireContext())
        }
        findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
    }
}
