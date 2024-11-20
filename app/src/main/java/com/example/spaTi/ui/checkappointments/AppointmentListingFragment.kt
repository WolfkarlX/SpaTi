package com.example.spaTi.ui.checkappointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentAppointmentListingBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AppointmentListingFragment : Fragment() {

    var position = -1
    val TAG: String = "AppointmentListinFragment"
    lateinit var binding: FragmentAppointmentListingBinding
    val viewModel: AppointmentViewModel by viewModels()
    val authViewModel: SpaAuthViewModel by viewModels()
    val adapter by lazy {
        AppointmentListingAdapter(
            onItemClicked = { pos, item, Buttontype ->
                position = pos
                Bundle().apply {
                    putParcelable("appointment",item)
                }
                when(Buttontype){
                    1 -> viewModel.setAppointmentAccepted(item.id)
                    0 -> viewModel.setAppointmentDeclined(item.id)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized){
            return binding.root
        }else {
            binding = FragmentAppointmentListingBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        ApointmentStatusObserver()

        val LinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewCitas.layoutManager = LinearLayoutManager
        binding.recyclerViewCitas.adapter = adapter
        viewModel.getAppointmentsBySpa()
    }

    private fun observer(){
        viewModel.appointmentsBySpa.observe(viewLifecycleOwner) { state ->
            when(state){
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

    private fun ApointmentStatusObserver(){
        viewModel.setAppointmentStatus.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    //observer()
                    toast(state.data)
                    //viewModel.getAppointmentsBySpa()
                    if (position != -1) {
                        adapter.removeItem(position)
                        position =-1
                    }
                }
            }
        }
    }

}