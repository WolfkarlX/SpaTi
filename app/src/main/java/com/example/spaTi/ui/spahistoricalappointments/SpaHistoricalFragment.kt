package com.example.spaTi.ui.spahistoricalappointments

import SpaHistoricalAdapter
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
import com.example.spaTi.databinding.FragmentSpaHistoricalBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class SpaHistoricalFragment : Fragment() {

    var position = -1
    val TAG: String = "SpaHistoricalFragment"
    lateinit var binding: FragmentSpaHistoricalBinding
    val viewModel: AppointmentViewModel by viewModels()
    val authViewModel: SpaAuthViewModel by viewModels()
    private var CurrentDate : LocalDate? = null
    private var CurrentHour: LocalTime? = null
    private var objSpa: Spa? = null


    val adapter by lazy {
        SpaHistoricalAdapter { pos, appointment ->
            val bundle = Bundle().apply {
                putParcelable("appointment", appointment)
                putInt("position", pos)
                putParcelable("spa", objSpa) // Ensure `objSpa` is Parcelable
            }
            findNavController().navigate(R.id.action_spaHistoricalFragment_to_spaReportsDetailing, bundle)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized){
            return binding.root
        }else {
            binding = FragmentSpaHistoricalBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        //ApointmentStatusObserver()

        val LinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.appointmentsRecyclerView.layoutManager = LinearLayoutManager
        binding.appointmentsRecyclerView.adapter = adapter

        objSpa = arguments?.getParcelable<Spa>("spa")

        updateDayNhour()

        viewModel.getAppointmentsHistory(objSpa!!.id, CurrentDate!!, CurrentHour!!)
    }

    private fun updateDayNhour() {
        // Get the current date and time
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()

        // Assign values to CurrentDate and CurrentHour
        CurrentDate = currentDate // Assign LocalDate object
        CurrentHour = currentTime // Change CurrentHour to LocalTime type
    }


    private fun observer(){
        viewModel.getAppointmentHistory.observe(viewLifecycleOwner) { state ->
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
                    val appointments = state.data
                    if (appointments.isNotEmpty()) {
                        adapter.updateList(state.data)
                    }

                }
            }
        }
    }

    /*private fun ApointmentStatusObserver(){
        viewModel.setAppointmentStatus.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.appointmentsRecyclerView.show()
                }
                is UiState.Failure -> {
                    binding.appointmentsRecyclerView.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.appointmentsRecyclerView.hide()
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
    }*/

}