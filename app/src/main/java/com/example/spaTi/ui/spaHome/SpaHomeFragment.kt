package com.example.spaTi.ui.spaHome

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentSpaHomeBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpaHomeFragment : Fragment() {
    val TAG: String = "SpaHomeFragment"
    val authViewModel: SpaAuthViewModel by viewModels()
    val viewModel: AppointmentViewModel by viewModels()

    private var _binding: FragmentSpaHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpaHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appointmentsObserver()

        binding.services.setOnClickListener {
            findNavController().navigate(R.id.action_spaHomeFragment_to_servicesFragment)
        }
        binding.logout.setOnClickListener {
            authViewModel.logout {
                findNavController().navigate(R.id.action_spaHomeFragment_to_loginFragment)
            }
        }
        binding.configuration.setOnClickListener {
            findNavController().navigate(R.id.action_spaHomeFragment_to_myaccountspaFragment)
        }
        binding.solicitud.setOnClickListener {
            findNavController().navigate(R.id.action_spaHomeFragment_to_appointmentlisitngFragment)
        }
    }

    private fun appointmentsObserver(){
        viewModel.checkPendingAppointments.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart(){
        super.onStart()
        authViewModel.getSession {
            viewModel.checkPendingAppointments(it?.id.toString())
        }
    }
}