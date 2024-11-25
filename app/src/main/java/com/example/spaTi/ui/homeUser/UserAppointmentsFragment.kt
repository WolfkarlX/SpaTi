package com.example.spaTi.ui.homeUser

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.spaTi.R
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Report
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.BottomSheetReportFragmentBinding
import com.example.spaTi.databinding.FragmentUserAppointmentsBinding
import com.example.spaTi.ui.Profile.ProfileViewModel
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.ui.spahistoricalappointments.ReportValidator
import com.example.spaTi.ui.spahistoricalappointments.ReportsViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.toastShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
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
    private val reportViewModel: ReportsViewModel by activityViewModels()
    private var isSessionLoaded = false
    private var tabSelected = 0
    private var snapHelper: PagerSnapHelper? = null
    private var reportBottomSheet: BottomSheetDialog? = null
    private val reportValidator = ReportValidator()
    val userAppointmentsAdapter by lazy {
        UserAppointmentsAdapter(
            onItemClicked = { _, item, buttonType ->
                when(buttonType){
                    1 -> appointmentViewModel.setAppointmentCanceled(item.id)
                    2 -> serviceViewModel.getServiceById(item.serviceId)
                    3 -> showBottomSheetReportSpa(item)
                    4 -> showConfirmDeleteReportDialog(item)
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
        reportBottomSheet?.dismiss()
        reportBottomSheet = null
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

        // Setup RecyclerView
        binding.appointmentScheduleRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false )
            adapter = userAppointmentsAdapter
            setHasFixedSize(true)

            snapHelper?.attachToRecyclerView(null)
            snapHelper = PagerSnapHelper()
            snapHelper?.attachToRecyclerView(this)
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
        setupListeners()
        observeAppointmentOperations()
        observeServiceOperations()
        observeReportOperations()
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

    private fun setupListeners() {
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
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        if (isSessionLoaded) {
                            tabSelected = 0
                            updateViewOnTab(tabSelected)
                            currentMonth = YearMonth.now()
                            updateCalendarMonth()

                            dateSelected?.let { binding.appointmentCalendar.resetDate(it) }
                            dateSelected = MonthDay.now().atYear(LocalDate.now().year)
                            binding.appointmentCalendar.markSelected(dateSelected!!)
                            userAppointmentsAdapter.clearAppointments()
                            objUser?.let { user ->
                                appointmentViewModel.getAppointmentsByDateAndUser(user.id, dateSelected!!)
                            }
                        }
                    }
                    1 -> {
                        if (isSessionLoaded) {
                            tabSelected = 1
                            updateViewOnTab(tabSelected)
                            userAppointmentsAdapter.clearAppointments()

                            objUser?.let { user ->
                                appointmentViewModel.getAppointmentsUserHistory(user.id)
                            }
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateViewOnTab(state: Int) {
        when(state) {
            0 -> {
                binding.appointmentCalendar.visibility = View.VISIBLE
                binding.appointmentCalendarMonth.visibility = View.VISIBLE
                binding.appointmentCalendarMonthNext.visibility = View.VISIBLE
                binding.appointmentCalendarMonthPrev.visibility = View.VISIBLE

                binding.appointmentScheduleRecycler.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false )
                    adapter = userAppointmentsAdapter
                    setHasFixedSize(true)
                    // Detach existing snapHelper if any
                    snapHelper?.attachToRecyclerView(null)
                    // Create and attach new snapHelper only for horizontal layout
                    snapHelper = PagerSnapHelper()
                    snapHelper?.attachToRecyclerView(this)
                }
            }
            1 -> {
                binding.appointmentCalendar.visibility = View.GONE
                binding.appointmentCalendarMonth.visibility = View.GONE
                binding.appointmentCalendarMonthNext.visibility = View.GONE
                binding.appointmentCalendarMonthPrev.visibility = View.GONE

                binding.appointmentScheduleRecycler.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false )
                    adapter = userAppointmentsAdapter
                    setHasFixedSize(true)
                    // Detach snapHelper for vertical layout since we don't need it
                    snapHelper?.attachToRecyclerView(null)
                    snapHelper = null
                }
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
        appointmentViewModel.getAppointmentsUserHistory.observe(viewLifecycleOwner) { state ->
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
                    Log.d("getAppointmentsUserHistory", state.data.toString())
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                    Log.d("getAppointmentsUserHistory", state.error.toString())
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
                    reportBottomSheet = null
                    destroyObservers()
                }
            }
        }
    }

    private fun observeReportOperations() {
        reportViewModel.reportSpaAction.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.show()
                is UiState.Success -> {
                    binding.appointmentProgressBar.hide()
                    reportBottomSheet?.dismiss()
                    reportBottomSheet = null

                    tabSelected = 1
                    updateViewOnTab(tabSelected)
                    userAppointmentsAdapter.clearAppointments()

                    objUser?.let { user ->
                        appointmentViewModel.getAppointmentsUserHistory(user.id)
                    }
                    toast(state.data)
                }
                is UiState.Failure -> {
                    binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
            }
        }
    }

    private fun destroyObservers() {
        serviceViewModel.getServiceById.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentByMonth.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentsByDateOnSpaSchedule.removeObservers(viewLifecycleOwner)
        appointmentViewModel.setAppointmentStatus.removeObservers(viewLifecycleOwner)
        appointmentViewModel.getAppointmentsUserHistory.removeObservers(viewLifecycleOwner)
        reportViewModel.reportSpaAction.removeObservers(viewLifecycleOwner)
    }

    private fun showBottomSheetReportSpa(appointment: Appointment) {
        reportBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            val bottomSheetBinding = BottomSheetReportFragmentBinding.inflate(layoutInflater)
            setContentView(bottomSheetBinding.root)

            // Get the bottom sheet behavior
            val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // Set window to adjust resize
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                isDraggable = true
                isHideable = true
                // Skip half-expanded state
                skipCollapsed = true
            }

            // Adjust the bottom sheet when keyboard shows/hides
            bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
                val rect = Rect()
                bottomSheet.getWindowVisibleDisplayFrame(rect)
                val screenHeight = bottomSheet.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) { // Keyboard is visible
                    bottomSheet.layoutParams.height = screenHeight - keypadHeight
                    bottomSheet.requestLayout()
                } else { // Keyboard is hidden
                    bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    bottomSheet.requestLayout()
                }
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
                // Make EditText focused when bottom sheet opens
                reportReason.requestFocus()

                btnSubmitReport.setOnClickListener {
                    val result = reportValidator.validate(reportReason.text.toString())
                    if (result.isValid) {
                        reportViewModel.reportSpaAction(Report(
                            id = "",
                            spaId = appointment.spaId,
                            userId = appointment.userId,
                            reason = reportReason.text.toString(),
                        ))
                    } else {
                        result.errorMessageResId?.let { toast(getString(it)) }
                    }
                }
            }

            show()
        }
    }

    private fun showConfirmDeleteReportDialog(appointment: Appointment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Borrar Reporte") // Add this string resource
            .setMessage("Seguro que quiere borrar este reporte?") // Add this string resource
            .setPositiveButton("Delete") { dialog, _ ->
                reportViewModel.reportSpaAction(Report(
                    id = "",
                    spaId = appointment.spaId,
                    userId = appointment.userId,
                    reason = "",
                ))
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}