package com.example.spaTi.ui.spaSchedule

import SpaScheduleAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentSpaScheduleBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.homeUser.ImageViewerUtil
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth


class SpaScheduleFragment : Fragment() {
    private var _binding: FragmentSpaScheduleBinding? = null
    private val binding get() = _binding!!
    private var currentMonth = YearMonth.now()
    private var objSpa: Spa? = null
    private var dateSelected : LocalDate? = null
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()
    private val serviceViewModel: ServiceViewModel by activityViewModels()
    var position = -1
    private var isBottomSheetShowing = false
    private var currentBottomSheet: BottomSheetDialog? = null
    val adapterSchedule by lazy {
        SpaScheduleAdapter(
            onItemClicked = { pos, item, buttonType, url ->
                position = pos
                when(buttonType){
                    1 -> appointmentViewModel.setAppointmentDeclined(item.id)
                    2 -> openImage(url)
                    3 -> openPdf(url)
                }
            }
        )
    }

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
                is UiState.Loading -> binding.appointmentProgressBar.hide()
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


                }
                is UiState.Failure -> {
                    //binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
            }
        }
        appointmentViewModel.getAppointmentsByDateOnSpaSchedule.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.appointmentProgressBar.hide()
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
        appointmentViewModel.setAppointmentStatus.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    //binding.appointmentProgressBar.show()
                }
                is UiState.Failure -> {
                    //binding.appointmentProgressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    //binding.appointmentProgressBar.hide()
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
    }


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


    private fun openImage(url: String) {
        lifecycleScope.launch {
            try {
                // Download the image and get a local URI
                val imageUri = downloadAndSaveImage(url)

                // If download is successful, show the image
                imageUri?.let { uri ->
                    ImageViewerUtil.showImage(
                        context = requireContext(),
                        uri = uri,
                        lifecycleScope = lifecycleScope,
                        onError = { errorMessage ->
                            toast(errorMessage)
                            Log.d("openImage", errorMessage)
                        }
                    )
                } ?: run {
                    toast("Failed to download image")
                }
            } catch (e: Exception) {
                toast("Error: ${e.localizedMessage}")
                Log.e("openImage", "Error downloading image", e)
            }
        }
    }

    private suspend fun downloadAndSaveImage(imageUrl: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // Use URL connection to download the image
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            // Read the input stream
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Save bitmap to external files directory
            val filename = "${System.currentTimeMillis()}_downloaded_image.jpg"
            val file = File(requireContext().getExternalFilesDir(null), filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // Convert file to Uri using Uri.fromFile
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("downloadImage", "Error downloading image", e)
            null
        }
    }


    private fun openPdf(url: String) {
        try {
            // Option 1: Using an external PDF viewer
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(url.toUri(), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if there's an app to handle PDF
            val packageManager = requireContext().packageManager
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Option 2: If no PDF viewer is found, prompt to download one
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("PDF Viewer Not Found")
                    .setMessage("Would you like to download a PDF viewer from the Play Store?")
                    .setPositiveButton("Yes") { _, _ ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.adobe.reader")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        } catch (e: Exception) {
            toast("Error opening PDF: ${e.localizedMessage}")
        }
    }
}