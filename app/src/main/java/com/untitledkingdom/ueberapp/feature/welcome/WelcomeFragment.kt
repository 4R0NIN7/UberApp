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
import com.untitledkingdom.ueberapp.feature.MyViewModel
import com.untitledkingdom.ueberapp.feature.state.MyEffect
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

@FlowPreview
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
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
                WelcomeScreen(myViewModel.processor)
            }
        }
    }

    private fun viewEvents() = listOf(
        flowOf(MyEvent.SetIsClickable(true))
    )

    private fun trigger(effect: MyEffect) {
        when (effect) {
            is MyEffect.ConnectToDevice -> goToMainFragment()
            is MyEffect.ShowError -> showError(effect.message)
            else -> {}
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error check logs!", Toast.LENGTH_SHORT).show()
        Timber.d(message = message)
    }

    private fun goToMainFragment() {
        Toast.makeText(requireContext(), "Successfully connected to device!", Toast.LENGTH_SHORT)
            .show()
        findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
    }
}
