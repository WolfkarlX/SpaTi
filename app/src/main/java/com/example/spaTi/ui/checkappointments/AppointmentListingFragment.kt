package com.example.spaTi.ui.checkappointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
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

    val TAG: String = "AppointmentListinFragment"
    lateinit var binding: FragmentAppointmentListingBinding
    val viewModel: AppointmentViewModel by viewModels()
    val authViewModel: SpaAuthViewModel by viewModels()
    val adapter by lazy {
        AppointmentListingAdapter(
            onItemClicked = { pos, item ->
                Bundle().apply {
                    putParcelable("note",item)
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
        oberver()

        val LinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewCitas.layoutManager = LinearLayoutManager
        binding.recyclerViewCitas.adapter = adapter
//        binding.button.setOnClickListener {
//            findNavController().navigate(R.id.action_noteListingFragment_to_noteDetailFragment)
//        }
//        binding.logout.setOnClickListener {
//            authViewModel.logout {
//                findNavController().navigate(R.id.action_noteListingFragment_to_loginFragment)
//            }
//        }
//        binding.profileButton.setOnClickListener{
//            findNavController().navigate(R.id.action_noteListingFragment_to_myprofileFragment)
//
//        }
//
//        // MOVE THIS BINDING TO MOVE THE SERVICES CRUD
//        binding.testButton.setOnClickListener {
//            findNavController().navigate(R.id.action_noteListingFragment_to_servicesFragment)
//        }
        viewModel.getAppointmentsBySpa()
    }

    private fun oberver(){
        viewModel.appointmentsBySpa.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                    Log.d("XDDD", state.error.toString())
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    adapter.updateList(state.data.toMutableList())
                }
            }
        }
    }
}