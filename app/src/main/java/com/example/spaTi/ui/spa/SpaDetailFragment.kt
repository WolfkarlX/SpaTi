package com.example.spaTi.ui.spa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spaTi.R
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentSpaDetailBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpaDetailFragment : Fragment() {
    private val viewModel: SpaViewModel by viewModels()
    private var _binding: FragmentSpaDetailBinding? = null
    private val binding get() = _binding!!
    private var isFavorite: Boolean = false
    private var objSpa: Spa? = null

    val servicesAdapter by lazy {
        SpaServicesAdapter { _, item ->
            Bundle().apply {
                putParcelable("service", item)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
        objSpa?.let { viewModel.getServicesBySpaId(it.id) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupObservers()
        _binding = null
    }

    private fun setupViews() {
        setupRecyclerView()
        setupFragmentData()
        setupClickListeners()
    }

    private fun setupFragmentData() {
        val spaFromRegular = arguments?.getParcelable<Spa>("spa")
        val spaFromFavorites = arguments?.getParcelable<Spa>("spaFavorites")

        val spaFromArgs = when {
            spaFromRegular != null -> {
                isFavorite = false
                objSpa = spaFromRegular
                spaFromRegular
            }
            spaFromFavorites != null -> {
                isFavorite = true
                objSpa = spaFromFavorites
                spaFromFavorites
            }
            else -> null
        }

        spaFromArgs?.let { spa ->
//            binding.spaDetailImage // here you set the image of the spa
            binding.spaDetailName.text = spa.spa_name
            binding.spaDetailLocation.text = spa.location
            if (!isFavorite) {
                binding.spaDetailFavBtn.setColorFilter(resources.getColor(R.color.white))
            }
        } ?: run {
            toast("Error: No spa data found")
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.spaDetailServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = servicesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.spaDetailBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.spaDetailFavBtn.setOnClickListener {
            // TODO: Set the favorites feature here
        }
    }

    private fun observeViewModels() {
        observeServiceOperations()
    }

    private fun observeServiceOperations() {
        viewModel.getServicesBySpaId.observe(viewLifecycleOwner) { state ->
            handleServiceState(state)
        }
    }

    private fun handleServiceState(state: UiState<List<Service>>) {
        when (state) {
            is UiState.Loading -> binding.progressBar.show()
            is UiState.Success -> {
                binding.progressBar.hide()
                servicesAdapter.updateItems(state.data.toMutableList())
            }
            is UiState.Failure -> {
                binding.progressBar.hide()
                toast(state.error)
            }
        }
    }

    private fun cleanupObservers() {
        viewModel.getServicesBySpaId.removeObservers(viewLifecycleOwner)
    }
}