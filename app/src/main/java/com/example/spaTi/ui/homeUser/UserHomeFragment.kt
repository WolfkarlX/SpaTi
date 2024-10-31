package com.example.spaTi.ui.homeUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentUserHomeBinding
import com.example.spaTi.ui.spa.SpaViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserHomeFragment : Fragment() {
    private val viewModel: SpaViewModel by viewModels()
    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

    val spasAdapter by lazy {
        SpasAdapter { _, item ->
            findNavController().navigate(R.id.action_userHomeFragment_to_spaDetailFragment, Bundle().apply {
                putParcelable("spa", item)
            })
        }
    }

    val spasFavoritesAdapter by lazy {
        SpasAdapter { _, item ->
            findNavController().navigate(R.id.action_userHomeFragment_to_spaDetailFragment, Bundle().apply {
                putParcelable("spaFavorites", item)
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
        viewModel.getSpas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupObservers()
        _binding = null
    }

    private fun setupViews() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.userHomeHomeRecyclerAll.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = spasAdapter
            setHasFixedSize(true) // Optimization when items have fixed dimensions
        }
        binding.userHomeHomeRecyclerAll.adapter = spasAdapter

        // Setup for Carousel (favorites)
        binding.userHomeHomeRecyclerFavorites.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = spasFavoritesAdapter
            setHasFixedSize(true)

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }
    }

    private fun observeViewModels() {
        observeSpaOperations()
    }

    private fun observeSpaOperations() {
        viewModel.spas.observe(viewLifecycleOwner) { state ->
            handleSpaState(state)
        }
    }

    private fun handleSpaState(state: UiState<List<Spa>>) {
        when (state) {
            is UiState.Loading -> binding.progressBar.show()
            is UiState.Success -> {
                binding.progressBar.hide()
                spasAdapter.updateItems(state.data.toMutableList())
                spasFavoritesAdapter.updateItems(state.data.toMutableList())
            }
            is UiState.Failure -> {
                binding.progressBar.hide()
                toast(state.error)
            }
        }
    }

    private fun cleanupObservers() {
        viewModel.spas.removeObservers(viewLifecycleOwner)
    }
}