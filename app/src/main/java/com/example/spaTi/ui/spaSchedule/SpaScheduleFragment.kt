package com.example.spaTi.ui.spaSchedule

import SpaScheduleAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentSpaScheduleBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar


class SpaScheduleFragment : Fragment() {
    private var _binding: FragmentSpaScheduleBinding? = null
    private val binding get() = _binding!!
    private var currentMonth = YearMonth.now()
    private var objSpa: Spa? = null
    private var objService: Service? = null
    private var dateSelected : LocalDate? = null
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()
    private val serviceViewModel: ServiceViewModel by activityViewModels()
    var position = -1
    private var isBottomSheetShowing = false
    private var currentBottomSheet: BottomSheetDialog? = null
    val adapterSchedule by lazy {
        SpaScheduleAdapter(
            onItemClicked = { pos, item, Buttontype ->
                position = pos
                when(Buttontype){
                    1 -> appointmentViewModel.setAppointmentDeclined(item.id)
                }
            }
        )
    }
    private var currentAppointments = listOf<Appointment>()
    private val serviceCache = mutableMapOf<String, Service>()
    private var pendingServiceRequests = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpaScheduleBinding.inflate(layoutInflater)
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
        adapterSchedule.clearAppointments()
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
        appointmentViewModel.getAppointmentByMonth.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.show()
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    val appointmentsByDate = state.data
                    var counter = 0

                    for ((date, appointments) in appointmentsByDate) {
                        counter += appointments.size
                        when (appointments.size) {
                            in 1..1 -> binding.appointmentCalendar.therearefeAppointments(date)
                            in 2 .. 3 -> binding.appointmentCalendar.markBusy(date)
                            in 4..Int.MAX_VALUE -> binding.appointmentCalendar.markEmpty(date)
                        }
                    }

                    Log.d("XDDD", counter.toString())

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
                        processAppointments(appointments)
                    }
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }

            }
        }
        //YOU WERE WORKING HERE YESTERDAY
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
                    if (position != -1) {
                        //updateCalendarMonth()
                        val ListCount = adapterSchedule.itemCount
                        if(ListCount < 2){
                            updateCalendarMonth()
                            return@observe
                        }
                        adapterSchedule.removeItem(position)
                        updateAppointmentsDays()
                        position =-1
                        toast(state.data)
                    }
                }
            }
        }
    }

    private fun updateAppointmentsDays() {
        appointmentViewModel.getAppointmentByMonthOnAppointmentsSchedule(objSpa!!.id, currentMonth)
    }

    private fun processAppointments(appointments: List<Map<String, Any>>) {
        adapterSchedule.updateList(appointments)
        /*currentAppointments = appointments
        serviceCache.clear()
        pendingServiceRequests = appointments.size

        // Fetch service details for each appointment
        appointments.forEach { appointment ->
            serviceViewModel.getServiceById(appointment.serviceId)
        }*/

    }

    /*private fun updateScheduleWithServices() {
        val occupiedSlots = currentAppointments.mapNotNull { appointment ->
            val service = serviceCache[appointment.serviceId]
            if (service != null) {
                Pair(appointment.dateTime, service.durationMinutes)
            } else null
        }
        /*adapterSchedule.setTimeRange(
            startTime = objSpa!!.inTime,
            endTime = objSpa!!.outTime,
            serviceDurationMinutes = 10/*objService!!.durationMinutes*/,
            occupiedSlots = occupiedSlots
        )*/
    }*/

    private fun setupRecyclerView() {

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.appointmentScheduleRecycler)

        binding.appointmentScheduleRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false )
            adapter = adapterSchedule
            setHasFixedSize(true)
        }
    }

    private fun setupFragmentData() {
        objSpa = arguments?.getParcelable<Spa>("spa")
        //objService = arguments?.getParcelable<Spa>("services")

        /*objSpa?.let { binding.appointmentSpaName.text = it.spa_name }
        objService?.let {
            binding.appointmentServiceSelected.text = "${it.name} - ${convertMinutesToReadableTime(it.durationMinutes)} - ${it.price} MXN"
        }*/

        updateCalendarMonth()
    }

    private fun setupClickListeners() {
        binding.appointmentCalendar.setOnDateClickListener { date ->
            dateSelected?.let { binding.appointmentCalendar.resetDate(it) }
            dateSelected = date
            binding.appointmentCalendar.markSelected(dateSelected!!)
            adapterSchedule.clearAppointments()
            appointmentViewModel.getAppointmentsByDateOnAppointmentsSchedule(objSpa!!.id, dateSelected!!)
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
        appointmentViewModel.getAppointmentByMonthOnAppointmentsSchedule(objSpa!!.id, currentMonth)
        adapterSchedule.clearAppointments()
    }


    /*private fun showConfirmBottomSheet(date: String) {
        isBottomSheetShowing = true

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            setOnDismissListener {
                isBottomSheetShowing = false
                currentBottomSheet = null
            }

            val bottomSheetBinding = FragmentAppointmentConfirmBinding.inflate(LayoutInflater.from(context))
            setContentView(bottomSheetBinding.root)

            val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
            bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.75).toInt()

            val behavior = BottomSheetBehavior.from(bottomSheet ?: return@apply)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.isHideable = true

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            with(bottomSheetBinding) {
                appointmentConfirmName.text = objSpa?.spa_name ?: "Spa name unavailable"
                appointmentConfirmService.text = objService?.name ?: "Service name unavailable"
                appointmentConfirmDate.text = dateSelected?.format(
                    DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
                ) ?: "Date unavailable"

                val dateEnd = try {
                    objService?.let {
                        LocalTime.parse(date.trim(), DateTimeFormatter.ofPattern("HH:mm"))
                            .plusMinutes(it.durationMinutes.toLong())
                    }
                } catch (e: Exception) {
                    null
                }

                appointmentConfirmTime.text = "$date - ${dateEnd ?: "End time unavailable"}"

                appointmentConfirmCancelBtn.setOnClickListener {
                    dismiss()
                }
            }
        }

        currentBottomSheet = bottomSheetDialog
        bottomSheetDialog.show()
    }*/
}