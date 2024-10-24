package com.example.spaTi.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentServicesBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

/**
 * ServicesFragment displays a list of services and allows navigation to
 * the detail view for each service. It handles the retrieval of services
 * through the ServiceViewModel and observes the loading state to update
 * the UI accordingly. The user can click on a service to view or edit
 * its details.
 *
 * The fragment has the following key features:
 * - Uses a staggered grid layout for displaying services.
 * - Navigates to ServiceDetailFragment when a service is clicked.
 * - Observes service loading states and updates the UI based on the state.
 *
 * Lifecycle methods:
 * - [onCreateView]: Inflates the fragment's view and initializes binding.
 * - [onViewCreated]: Sets up the observer and initializes the RecyclerView.
 * - [observer]: Watches the view model for updates to show progress, errors, or the service list.
 */
@AndroidEntryPoint
class ServicesFragment : Fragment() {
    val TAG: String = "ServicesFragment"
    val viewModel: ServiceViewModel by viewModels()
    val adapter by lazy {
        ServicesAdapter { pos, item ->
            if ( item == null ){
                findNavController().navigate(R.id.action_servicesFragment_to_serviceDetailFragment)
            } else {
                findNavController().navigate(R.id.action_servicesFragment_to_serviceDetailFragment, Bundle().apply {
                    putParcelable("service", item)
                })
            }
        }
    }

    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        binding.recyclerView.adapter = adapter
        binding.home.setOnClickListener {
            findNavController().navigate(R.id.action_servicesFragment_to_spaHomeFragment)
        }
        viewModel.getServices()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.service.removeObservers(viewLifecycleOwner)

        _binding = null
    }

    private fun observer() {
        viewModel.service.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    adapter.updateList(state.data.toMutableList())
                }
            }
        }
    }
}