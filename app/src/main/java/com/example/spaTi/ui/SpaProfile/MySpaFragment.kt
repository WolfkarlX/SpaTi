package com.example.spaTi.ui.SpaProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentMySpaBinding
import com.example.spaTi.ui.Profile.ProfileViewModel
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.ui.spa.SpaViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySpaFragment : Fragment() {
    val TAG: String = "MyspaFragment"
    val viewModel: MySpaViewModel by viewModels()

    private var _binding: FragmentMySpaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMySpaBinding.inflate(layoutInflater)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //Do nothing
            }
        })

        observer()
        viewModel.getSession()

        binding.home.setOnClickListener {
            findNavController().navigate(R.id.action_myspaFragment_to_myaccountspaFragment)
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_myspaFragment_to_myspaeditFragment)
        }
    }

    fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress or loading indicator
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    // Hide progress and show error message
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.sessionProgress.hide()
                    setData(state.data) // Call setData to update UI with user info
                }
            }
        }
    }

    // Update the UI with user session data
    fun setData(spa: Spa?) {
        spa?.let {
            binding.nameSpa.setText(it.spa_name)
            binding.locationSpa.setText(it.location)
            binding.phoneSpa.setText(it.cellphone)
            binding.apertura.setText(it.inTime)
            binding.cierre.setText(it.outTime)
            binding.descriptionSpa.setText(it.description)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}