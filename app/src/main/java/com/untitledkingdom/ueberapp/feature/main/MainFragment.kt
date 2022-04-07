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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.MyViewModel
import com.untitledkingdom.ueberapp.feature.state.MyEffect
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.utils.showError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf

@ExperimentalPagerApi
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : Fragment() {
    private val myViewModel: MyViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onProcessor(
            lifecycleState = Lifecycle.State.RESUMED,
            processor = myViewModel::processor,
            onEffect = ::trigger,
            viewEvents = ::viewEvents
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                MainScreenCompose(myViewModel.processor)
            }
        }
    }

    private fun viewEvents() = listOf(
        flowOf(MyEvent.SetIsClickable(true))
    )

    private fun trigger(effect: MyEffect) {
        when (effect) {
            MyEffect.GoToWelcome -> goToWelcome()
            is MyEffect.ShowError -> showError(message = effect.message, context = requireContext())
            else -> {}
        }
    }

    private fun goToWelcome() {
        findNavController().navigate(R.id.action_mainFragment_to_welcomeFragment)
    }
}
