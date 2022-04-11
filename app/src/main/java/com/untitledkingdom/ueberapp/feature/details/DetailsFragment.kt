package com.untitledkingdom.ueberapp.feature.details

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
import com.untitledkingdom.ueberapp.feature.main.MainViewModel
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.utils.functions.toastMessage
import kotlinx.coroutines.FlowPreview

@FlowPreview
class DetailsFragment : Fragment() {
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
        )
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                DetailsScreen(viewModel.processor)
            }
        }
    }

    private fun trigger(effect: MainEffect) {
        when (effect) {
            is MainEffect.ShowData -> toastMessage(
                message = effect.data,
                context = requireContext()
            )
            is MainEffect.ShowError -> toastMessage(
                message = effect.message,
                context = requireContext()
            )
            MainEffect.GoBack -> findNavController().popBackStack()
            else -> {}
        }
    }
}