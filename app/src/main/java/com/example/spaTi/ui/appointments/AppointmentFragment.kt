package com.example.spaTi.ui.appointments

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
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
import com.example.spaTi.util.disable
import com.example.spaTi.util.enabled
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
    private lateinit var fileUploadHelper: FileUploadHelper
    private lateinit var uriReceiptUploaded: Pair<Uri?, String>

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
        uriReceiptUploaded = Pair(null, "")
        cleanupObservers()
        appointmentViewModel.resetAddAppointmentState()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
        setUpFileUploader()
    }

    override fun onResume() {
        super.onResume()
        adapterSchedule.clearTimeSlots()
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fileUploadHelper.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun setUpFileUploader() {
        fileUploadHelper = createFileUploadHelper(
            onImagePickingStarted = {
                binding.appointmentProgressBar.visibility = View.VISIBLE
            },
            onImagePickingCancelled = { errorCode ->
                binding.appointmentProgressBar.visibility = View.GONE
                when(errorCode) {
                    204 -> toast("Image picking was cancelled.")
                    300 -> toast("Camera permission denied. Please enable it to continue.")
                    301 -> toast("Camera permission denied. Please enable it to continue.")
                    400 -> toast("There was an error while uploading the file.")
                    404 -> toast("No image file was found.")
                    409 -> toast("The selected file format is not supported.")
                    415 -> toast("The selected file format is not supported.")
                    500 -> toast("An unknown error occurred.")
                    501 -> toast("The operation or feature is not supported in the current context.")
                    else -> toast("An unexpected error occurred.")
                }
                uploadImageFile(null)
            },
            handleFileResult = { uri, fileType ->
                when (fileType) {
                    FileUploadHelper.FileType.IMAGE_ONLY -> {
                        uploadImageFile(uri)
                    }
                    FileUploadHelper.FileType.PDF_ONLY -> {
                        uploadPdfFile(uri)
                    }
                    else -> toast("Upload a Image or PDF file.")
                }
                binding.appointmentProgressBar.visibility = View.GONE
            }
        ).apply {
            shouldCrop = false
            fileType = FileUploadHelper.FileType.IMAGE_OR_PDF
        }
    }

    private fun uploadImageFile(uri: Uri?) {
        // Find the current bottom sheet binding
        currentBottomSheet?.let { bottomSheetDialog ->
            bottomSheetDialog.findViewById<View>(R.id.fragment_appointment_confirm)?.let {
                val bottomSheetBinding = FragmentAppointmentConfirmBinding.bind(it)
                updateImageContainer(bottomSheetBinding, uri)
            }
        }
    }

    private fun updateImageContainer(binding: FragmentAppointmentConfirmBinding, uri: Uri?) {
        objSpa?.let { spa ->
            if (spa.prepayment) {
                uri?.let {
                    try {
                        binding.imageUploadedPreview.setImageURI(uri)
                        binding.imageUploadedPreview.visibility = View.VISIBLE

                        binding.imageUploadedIcon.visibility = View.INVISIBLE
                        binding.imageUploadedIconLabel.visibility = View.INVISIBLE
                        binding.imageUploadedIconLabel.text = "Adjuntar recibo"

                        binding.appointmentConfirmSubmitBtn.enabled()
                        context?.let {
                            binding.appointmentConfirmSubmitBtn.setBackgroundColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.verde
                                )
                            )
                        }
                        uriReceiptUploaded = Pair(it, "img")
                    } catch (e: Exception) {
                        Log.e("ImageUpload", "Error setting image URI", e)
                        toast("Failed to load image")
                    }
                }
            } else {
                // No prepayment required, hide upload icons
                binding.imageUploadedIcon.visibility = View.INVISIBLE
                binding.imageUploadedIconLabel.visibility = View.INVISIBLE
                binding.imageUploadedPreview.visibility = View.INVISIBLE
                binding.appointmentConfirmSubmitBtn.enabled()
                context?.let {
                    binding.appointmentConfirmSubmitBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            it,
                            R.color.verde
                        )
                    )
                }
            }
        }
    }

    private fun uploadPdfFile(uri: Uri) {
        currentBottomSheet?.let { bottomSheetDialog ->
            bottomSheetDialog.findViewById<View>(R.id.fragment_appointment_confirm)?.let {
                val bottomSheetBinding = FragmentAppointmentConfirmBinding.bind(it)
                updatePdfContainer(bottomSheetBinding, uri)
            }
        }
    }

    private fun updatePdfContainer(binding: FragmentAppointmentConfirmBinding, uri: Uri?) {
        objSpa?.let { spa ->
            if (spa.prepayment) {
                uri?.let {
                    try {
                        binding.imageUploadedPreview.setImageURI(null)
                        binding.imageUploadedPreview.visibility = View.INVISIBLE
                        binding.imageUploadedIcon.visibility = View.VISIBLE

                        // Add PDF file name
                        val fileName = getPdfFileName(uri)
                        binding.imageUploadedIconLabel.text = fileName
                        binding.imageUploadedIconLabel.visibility = View.VISIBLE

                        binding.appointmentConfirmSubmitBtn.enabled()
                        context?.let {
                            binding.appointmentConfirmSubmitBtn.setBackgroundColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.verde
                                )
                            )
                        }
                        uriReceiptUploaded = Pair(it, "pdf")
                    } catch (e: Exception) {
                        Log.e("PdfUpload", "Error handling PDF", e)
                        toast("Failed to load PDF")
                    }
                }
            } else {
                // No prepayment required, hide upload icons
                binding.imageUploadedPreview.visibility = View.INVISIBLE
                binding.imageUploadedIcon.visibility = View.INVISIBLE
                binding.imageUploadedIconLabel.visibility = View.INVISIBLE
                binding.appointmentConfirmSubmitBtn.enabled()
                context?.let {
                    binding.appointmentConfirmSubmitBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            it,
                            R.color.verde
                        )
                    )
                }
            }
        }
    }

    private fun getPdfFileName(uri: Uri): String {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } else {
                "Unknown File"
            }
        } ?: "Unknown File"
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

            behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                isDraggable = true
                isHideable = true
                // Skip half-expanded state
                skipCollapsed = true
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

                objSpa?.let {
                    if (it.prepayment) {
                        imageUploadedIcon.visibility = View.VISIBLE
                        imageUploadedIconLabel.visibility = View.VISIBLE
                        imageUploadedPreview.visibility = View.INVISIBLE

                        appointmentConfirmSubmitBtn.disable()
                        appointmentConfirmSubmitBtn.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.gray_400
                            )
                        )
                    } else {
                        // No prepayment required, hide upload icons
                        imageUploadedIcon.visibility = View.INVISIBLE
                        imageUploadedIconLabel.visibility = View.INVISIBLE
                        imageUploadedPreview.visibility = View.INVISIBLE
                    }
                }

                appointmentConfirmSubmitBtn.setOnClickListener {
                    dismiss()
                    val appointment = createAppointment(date.trim())
                    objSpa?.let {
                        if (it.prepayment) {
                            uriReceiptUploaded.first?.let { uri ->
                                appointmentViewModel.addAppointmentWithReceipt(appointment, uri, uriReceiptUploaded.second)
                            } ?: {
                                toast("This spa require you to prepay to schedule an appointment")
                            }
                        } else {
                            appointmentViewModel.addAppointment(appointment)
                        }
                    }
                }
                appointmentConfirmCancelBtn.setOnClickListener {
                    dismiss()
                }

                imageUploadedPreview.setOnClickListener {
                    fileUploadHelper.showImageSourceDialog()
                }
                imageUploadedIcon.setOnClickListener {
                    fileUploadHelper.showImageSourceDialog()
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