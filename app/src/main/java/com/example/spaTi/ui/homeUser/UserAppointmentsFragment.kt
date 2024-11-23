package com.example.spaTi.ui.homeUser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.spaTi.R
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentSpaScheduleBinding
import com.example.spaTi.databinding.FragmentUserAppointmentsBinding
import com.example.spaTi.ui.Profile.ProfileViewModel
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.ui.spa.SpaViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.convertMinutesToReadableTime
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.toastShort
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.MonthDay
import java.time.YearMonth

@AndroidEntryPoint
class UserAppointmentsFragment : Fragment() {
    private var _binding: FragmentUserAppointmentsBinding? = null
    private val binding get() = _binding!!
    private var currentMonth = YearMonth.now()
    private var objUser: User? = null
    private var dateSelected : LocalDate? = null
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()
    private val userViewModel: ProfileViewModel by activityViewModels()
    private val serviceViewModel: ServiceViewModel by activityViewModels()
    private var isSessionLoaded = false
    val userAppointmentsAdapter by lazy {
        UserAppointmentsAdapter(
            onItemClicked = { _, item, buttonType ->
                when(buttonType){
                    1 -> appointmentViewModel.setAppointmentCanceled(item.id)
                    2 -> serviceViewModel.getServiceById(item.serviceId)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAppointmentsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isSessionLoaded = false
        dateSelected = null
        destroyObservers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appointmentProgressBar.show()
        setupInitialViews()
        observeSession()
    }

    override fun onResume() {
        super.onResume()
        // Clear appointments and refresh the view when returning to this fragment
        if (isSessionLoaded) {
            binding.appointmentProgressBar.hide()
            userAppointmentsAdapter.clearAppointments()
            objUser?.let { user ->
                updateCalendarMonth()
                dateSelected?.let { date ->
                    appointmentViewModel.getAppointmentsByDateAndUser(user.id, date)
                }
            }
        }
    }

    private fun setupInitialViews() {
        binding.scheduleDay.text = "Mis reservaciones"
        binding.appointmentBackBtn.visibility = View.INVISIBLE

        // Setup RecyclerView
        binding.appointmentScheduleRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false )
            adapter = userAppointmentsAdapter
            setHasFixedSize(true)

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }
    }

    private fun observeSession() {
        userViewModel.syncSessionWithDatabase()

        userViewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.appointmentProgressBar.show()
                    disableInteractions()
                }
                is UiState.Success -> {
                    objUser = state.data
                    if (!isSessionLoaded) {
                        isSessionLoaded = true
                        setupAfterSessionLoad()
                    }
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                    userViewModel.logout {
                        findNavController().navigate(R.id.action_userAppointmentsFragment_to_loginFragment)
                    }
                }
            }
        }
    }

    private fun setupAfterSessionLoad() {
        setupClickListeners()
        observeAppointmentOperations()
        observeServiceOperations()
        enableInteractions()
        updateCalendarMonth()
        setTodayAsSelected()
    }

    private fun disableInteractions() {
        binding.appointmentCalendar.isEnabled = false
        binding.appointmentCalendarMonthNext.isEnabled = false
        binding.appointmentCalendarMonthPrev.isEnabled = false
        binding.appointmentScheduleRecycler.isEnabled = false
    }

    private fun enableInteractions() {
        binding.appointmentCalendar.isEnabled = true
        binding.appointmentCalendarMonthNext.isEnabled = true
        binding.appointmentCalendarMonthPrev.isEnabled = true
        binding.appointmentScheduleRecycler.isEnabled = true
    }

    private fun updateCalendarMonth() {
        binding.appointmentBackBtn.visibility = View.INVISIBLE
        binding.appointmentCalendar.setMonth(currentMonth)
        objUser?.let { user ->
            appointmentViewModel.getAppointmentByMonthAndUser(user.id, currentMonth)
            userAppointmentsAdapter.clearAppointments()
        }
    }

    private fun setTodayAsSelected() {
        if (isSessionLoaded) {
            dateSelected?.let { binding.appointmentCalendar.resetDate(it) }
            dateSelected = MonthDay.now().atYear(LocalDate.now().year)
            binding.appointmentCalendar.markSelected(dateSelected!!)
            userAppointmentsAdapter.clearAppointments()
            objUser?.let { user ->
                appointmentViewModel.getAppointmentsByDateAndUser(user.id, dateSelected!!)
            }
        }
    }

    private fun setupClickListeners() {
        binding.appointmentCalendar.setOnDateClickListener { date ->
            if (isSessionLoaded) {
                dateSelected?.let { binding.appointmentCalendar.resetDate(it) }
                dateSelected = date
                binding.appointmentCalendar.markSelected(dateSelected!!)
                userAppointmentsAdapter.clearAppointments()
                objUser?.let { user ->
                    appointmentViewModel.getAppointmentsByDateAndUser(user.id, dateSelected!!)
                }
            }
        }
        binding.appointmentCalendarMonthNext.setOnClickListener {
            if (isSessionLoaded) {
                currentMonth = currentMonth.plusMonths(1)
                updateCalendarMonth()
            }
        }
        binding.appointmentCalendarMonthPrev.setOnClickListener {
            if (isSessionLoaded) {
                currentMonth = currentMonth.minusMonths(1)
                updateCalendarMonth()
            }
        }
    }

    private fun observeAppointmentOperations() {
        appointmentViewModel.getAppointmentByMonth.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.show()
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    val appointmentsByDate = state.data
                    for ((date, appointments) in appointmentsByDate) {
                        when (appointments.size) {
                            in 1..1 -> binding.appointmentCalendar.therearefeAppointments(date)
                            in 2 .. 3 -> binding.appointmentCalendar.markBusy(date)
                            in 4..Int.MAX_VALUE -> binding.appointmentCalendar.markEmpty(date)
                        }
                    }
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
            }
        }
        appointmentViewModel.getAppointmentsByDateOnSpaSchedule.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.show()
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    val appointments = state.data
                    if (appointments.isNotEmpty()) {
                        userAppointmentsAdapter.updateList(appointments)
                    } else {
                        userAppointmentsAdapter.updateList(emptyList())
                    }
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }

            }
        }
        appointmentViewModel.setAppointmentStatus.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.appointmentProgressBar.show()
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    userAppointmentsAdapter.clearAppointments()
                    objUser?.let { user ->
                        appointmentViewModel.getAppointmentsByDateAndUser(user.id, dateSelected!!)
                    }
                    toast(state.data)
                }
            }
        }
    }

    private fun observeServiceOperations() {
        serviceViewModel.getServiceById.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.appointmentProgressBar.show()
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    userAppointmentsAdapter.clearAppointments()
                    findNavController().navigate(
                        R.id.action_userAppointmentsFragment_to_spaDetailFragment,
                        Bundle().apply { putParcelable("service", state.data) }
                    )
                    serviceViewModel.cleanGetServiceByIdState()
                    isSessionLoaded = false
                    dateSelected = null
                    destroyObservers()
                }
            }
        }
    }

    private fun destroyObservers() {
        serviceViewModel.getServiceById.removeObservers(viewLifecycleOwner)
        appointmentViewModel.setAppointmentStatus.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentsByDateOnSpaSchedule.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentByMonth.removeObservers(viewLifecycleOwner)
    }
}