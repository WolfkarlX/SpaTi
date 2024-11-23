package com.example.spaTi.ui.appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.spaTi.R
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentAppointmentBinding
import com.example.spaTi.databinding.FragmentAppointmentConfirmBinding
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.convertMinutesToReadableTime
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.toastShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppointmentFragment : Fragment() {
    private var _binding: FragmentAppointmentBinding? = null
    private val binding get() = _binding!!
    private var currentMonth = YearMonth.now()
    private var objSpa: Spa? = null
    private var objService: Service? = null
    private var dateSelected : LocalDate? = null
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()
    private val serviceViewModel: ServiceViewModel by activityViewModels()
    private var isBottomSheetShowing = false
    private var currentBottomSheet: BottomSheetDialog? = null
    private val adapterSchedule by  lazy {
        ScheduleAdapter { _, time, isValid, errorMessage ->
            if (!isBottomSheetShowing) {
                if (isValid) {
                    showConfirmBottomSheet(time)
                } else {
                    errorMessage?.let { toastShort(it) }
                }
            }
        }
    }

    private var currentAppointments = listOf<Appointment>()
    private val serviceCache = mutableMapOf<String, Service>()
    private var pendingServiceRequests = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBottomSheet?.dismiss()
        currentBottomSheet = null
        isBottomSheetShowing = false
        cleanupObservers()
        appointmentViewModel.resetAddAppointmentState()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
    }

    override fun onResume() {
        super.onResume()
        adapterSchedule.clearTimeSlots()
    }

    private fun setupViews() {
        setupRecyclerView()
        setupFragmentData()
        setupClickListeners()
    }

    private fun observeViewModels() {
        observeAppointmentOperations()
    }

    private fun observeAppointmentOperations() {
        appointmentViewModel.addAppointment.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    toast(state.data.second)
                    appointmentViewModel.resetAddAppointmentState()
                    findNavController().navigate(R.id.action_appointmentFragment_to_userAppointmentsFragment)
                }
                is UiState.Failure -> {
                    toast(state.error)
                }
            }
        }
        appointmentViewModel.getAppointmentByMonth.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.show()
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    val appointmentsByDate = state.data

                    for ((date, appointments) in appointmentsByDate) {
                        when (appointments.size) {
                            in 1..3 -> binding.appointmentCalendar.markBusy(date)
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
        appointmentViewModel.getAppointmentsByDate.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.show()
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    val appointments = state.data
                    if (appointments.isEmpty()) {
                        adapterSchedule.setTimeRange(
                            startTime = objSpa!!.inTime,
                            endTime = objSpa!!.outTime,
                            serviceDurationMinutes = objService!!.durationMinutes,
                            occupiedSlots = emptyList()
                        )
                    } else {
                        processAppointments(appointments)
                    }
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
            }
        }
        serviceViewModel.getServiceById.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    val service = state.data
                    serviceCache[service!!.id] = service
                    pendingServiceRequests--

                    if (pendingServiceRequests == 0) {
                        updateScheduleWithServices()
                    }
                }
                is UiState.Failure -> {
                    toast(state.error)
                    pendingServiceRequests--
                }
            }
        }
    }

    private fun processAppointments(appointments: List<Appointment>) {
        currentAppointments = appointments
        serviceCache.clear()
        pendingServiceRequests = appointments.size

        // Fetch service details for each appointment
        appointments.forEach { appointment ->
            serviceViewModel.getServiceById(appointment.serviceId)
        }
    }

    private fun updateScheduleWithServices() {
        val occupiedSlots = currentAppointments.mapNotNull { appointment ->
            val service = serviceCache[appointment.serviceId]
            if (service != null) {
                Pair(appointment.dateTime, service.durationMinutes)
            } else null
        }
        adapterSchedule.setTimeRange(
            startTime = objSpa!!.inTime,
            endTime = objSpa!!.outTime,
            serviceDurationMinutes = objService!!.durationMinutes,
            occupiedSlots = occupiedSlots
        )
    }

    private fun setupRecyclerView() {
        binding.appointmentScheduleRecycler.apply {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = adapterSchedule
            setHasFixedSize(true)
        }
    }

    private fun setupFragmentData() {
        objSpa = arguments?.getParcelable<Spa>("spa")
        objService = arguments?.getParcelable<Service>("service")

        objSpa?.let { binding.appointmentSpaName.text = it.spa_name }
        objService?.let {
            binding.appointmentServiceSelected.text = "${it.name} - ${convertMinutesToReadableTime(it.durationMinutes)} - ${it.price} MXN"
        }

        updateCalendarMonth()
    }

    private fun setupClickListeners() {
        binding.appointmentCalendar.setOnDateClickListener { date ->
            dateSelected?.let { binding.appointmentCalendar.resetDate(it) }
            dateSelected = date
            binding.appointmentCalendar.markSelected(dateSelected!!)
            appointmentViewModel.getAppointmentsByDate(objSpa!!.id, dateSelected!!)
        }
        binding.appointmentCalendarMonthNext.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendarMonth()
        }
        binding.appointmentCalendarMonthPrev.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateCalendarMonth()
        }
        binding.appointmentBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun cleanupObservers() {
        appointmentViewModel.addAppointment.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentByMonth.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentsByDate.removeObservers(viewLifecycleOwner)
        serviceViewModel.getServiceById.removeObservers(viewLifecycleOwner)
    }

    private fun updateCalendarMonth() {
        binding.appointmentCalendar.setMonth(currentMonth)
        appointmentViewModel.getAppointmentByMonth(objSpa!!.id, currentMonth)
        adapterSchedule.clearTimeSlots()
    }

    private fun showConfirmBottomSheet(date: String) {
        isBottomSheetShowing = true

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            setOnDismissListener {
                isBottomSheetShowing = false
                currentBottomSheet = null
            }

            val bottomSheetBinding = FragmentAppointmentConfirmBinding.inflate(layoutInflater)
            setContentView(bottomSheetBinding.root)

            val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            bottomSheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.75).toInt()

            behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                isDraggable = true
                isHideable = true
            }

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            with(bottomSheetBinding) {
                appointmentConfirmName.text = objSpa?.spa_name
                appointmentConfirmService.text = objService?.name
                appointmentConfirmDate.text = dateSelected?.format(
                    DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
                )
                val dateEnd = objService?.let {
                    LocalTime.parse(date.trim(), DateTimeFormatter.ofPattern("HH:mm"))
                        .plusMinutes(it.durationMinutes.toLong())
                }

                appointmentConfirmTime.text = "$date - $dateEnd"

                appointmentConfirmSubmitBtn.setOnClickListener {
                    dismiss()
                    val appointment = createAppointment(date.trim())
                    appointmentViewModel.addAppointment(appointment)
                }
                appointmentConfirmCancelBtn.setOnClickListener {
                    dismiss()
                }
            }
        }

        currentBottomSheet = bottomSheetDialog
        bottomSheetDialog.show()
    }

    private fun createAppointment(date: String): Appointment {
        return Appointment(
            id = "",
            spaId = objSpa!!.id,
            userId = "",
            serviceId = objService!!.id,
            date = dateSelected.toString(),
            dateTime = date,
            status = "pending",
        )
    }
}