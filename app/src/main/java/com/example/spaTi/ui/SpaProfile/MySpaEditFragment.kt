package com.example.spaTi.ui.SpaProfile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.SpaPrepayment
import com.example.spaTi.databinding.BottomSheetReportFragmentBinding
import com.example.spaTi.databinding.FragmentMySpaEditBinding
import com.example.spaTi.ui.spahistoricalappointments.ReportValidator
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MySpaEditFragment : Fragment() {

    companion object {
        const val REQUEST_CODE_LOCATION = 1001
    }

    var id = ""
    var email = ""
    var profileImageUrl = ""
    val TAG: String = "MySpaEditFragment"
    var createdAt: Date? = null
    val viewModel: MySpaViewModel by viewModels()
    private var reportBottomSheet: BottomSheetDialog? = null
    private val reportValidator = ReportValidator()
    private lateinit var spaObj: Spa
    private var spaPrepay: Pair<String, String> = Pair("", "")

    private var _binding: FragmentMySpaEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMySpaEditBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent back navigation
            }
        })

        observer()

        viewModel.getSession()

        binding.btnGuardar.setOnClickListener {
            if (validation()) {
                observeEdit()
                viewModel.editUser(spa = getUserObj())
            }
        }

        binding.btnCancelar.setOnClickListener {
            findNavController().navigate(R.id.action_myspaeditFragment_to_myspaFragment)
        }

        binding.locationSpaBtn.setOnClickListener {
            val currentLocation = binding.locationSpaBtn.text.toString()
            val intent = Intent(requireContext(), MapActivity3::class.java).apply {
                putExtra("spaLocation", currentLocation)
            }
            startActivityForResult(intent, REQUEST_CODE_LOCATION)
        }

        binding.prepaySwitch.setOnClickListener {
            if (binding.prepaySwitch.isChecked) {
                viewModel.getPrepayment(spaObj.id)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOCATION && resultCode == AppCompatActivity.RESULT_OK) {
            val newLocation = data?.getStringExtra("newLocation")
            newLocation?.let {
                binding.locationSpaBtn.setText(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        reportBottomSheet?.dismiss()
        reportBottomSheet = null
    }

    private fun getCoordinatesFromLocation(location: String): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocationName(location, 1)

        return if (!addresses.isNullOrEmpty()) {
            val latitude = addresses[0].latitude
            val longitude = addresses[0].longitude
            "Lat: $latitude, Lon: $longitude"
        } else {
            "Lat: 0.0, Lon: 0.0"
        }
    }

    fun validation(): Boolean {

        if (binding.spaNameEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_spaname))
            return false
        }

        if (binding.locationSpaBtn.text.isNullOrEmpty()){
            toast(getString(R.string.enter_location))
            return false
        }

        if (binding.telSpaEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_cellphone))
            return false
        } else {
            if (binding.telSpaEt.text.toString().length < 10 || binding.telSpaEt.text.toString().length > 15) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }

            val phoneNumber = binding.telSpaEt.text.toString()

            // Check if the number has at least three unique digits (to avoid repeated patterns like "3444444444")
            if (phoneNumber.toSet().size < 4) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }

            // Example pattern check: Ensure the number doesn't start with 0 or 1, common in some countries for invalid numbers
            if (phoneNumber.startsWith("0") || phoneNumber.startsWith("1")) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }

        }

        if(binding.inTimeEt.text.isNullOrEmpty()){
            toast(getString(R.string.input_hour))
            return false
        } else {
            val validTime = validateAndFormatTime(binding.inTimeEt.text.toString())

            if (validTime != null) {
                binding.inTimeEt.setText(validTime)
            } else {
                toast(getString(R.string.invalid_time_input))
                return false
            }
        }

        if(binding.outTimeEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_time_output))
            return false
        } else {
            val validTime = validateAndFormatTime(binding.outTimeEt.text.toString())

            if (validTime != null) {
                binding.outTimeEt.setText(validTime)
            } else {
                toast(getString(R.string.invalid_time_output))
                return false
            }
        }

        if (binding.descriptionEt.text.isNullOrEmpty()) {
            toast(getString(R.string.invalid_description_does_not_exists))
            return false
        } else {

            val description = binding.descriptionEt.text.toString()
            val consecutiveRepeat = Regex("(.)\\1{5,}")
            val descriptionRegex = Regex("^[a-zA-Z0-9.,!?'\"\\- ]{20,500}$")

            if (!description.matches(descriptionRegex)) {
                toast(getString(R.string.invalid_description_too_long))
                return false
            }
            if(consecutiveRepeat.containsMatchIn(description)){
                toast(getString(R.string.invalid_description_consecutive_repeated))
                return false
            }
        }

        return true
    }

    // Observe the session LiveData from the ViewModel
    private fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    setData(state.data)
                    spaObj = state.data
                }
            }
        }
        viewModel.getPrepayment.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    showBottomSheetReportSpa(state.data)
                }
            }
        }
    }

    private fun observeEdit() {
        viewModel.editUser.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    toast(state.data)
                    cleanInputs()

                    // Once the edit is successful, update the profile image URL
                    if (!profileImageUrl.isNullOrEmpty()) {
                        viewModel.updateProfileImage("spaId", Uri.parse(profileImageUrl))
                    }

                    if (spaPrepay.first.isNotEmpty() && spaPrepay.second.isNotEmpty()) {
                        viewModel.actionPrepay(spaObj.id, getUserPrepaymentObj(spaPrepay.first, spaPrepay.second.toDouble()))
                    } else {
                        findNavController().navigate(R.id.action_myspaeditFragment_to_myspaFragment)
                    }
                }
            }
        }
        viewModel.actionPrepay.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    toast(state.data.second)

                    findNavController().navigate(R.id.action_myspaeditFragment_to_myspaFragment)
                }
            }
        }
    }


    private fun setData(spa: Spa?) {
        spa?.let {
            email = it.email
            id = it.id
            profileImageUrl =
                it.profileImageUrl.toString()
            createdAt = it.createdAt

            binding.spaNameEt.setText(it.spa_name)
            binding.locationSpaBtn.setText(it.location)
            binding.telSpaEt.setText(it.cellphone)
            binding.inTimeEt.setText(it.inTime)
            binding.outTimeEt.setText(it.outTime)
            binding.descriptionEt.setText(it.description)
            binding.prepaySwitch.isChecked = it.prepayment
        }
    }

    private fun getUserObj(): Spa {

        val locationText = binding.locationSpaBtn.text.toString()
        val coordinates = getCoordinatesFromLocation(locationText)

        return Spa(
            id = id,
            spa_name = binding.spaNameEt.text.toString(),
            location = locationText,
            coordinates = coordinates,
            email = email,
            cellphone = binding.telSpaEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            type = "2",
            inTime = binding.inTimeEt.text.toString(),
            createdAt = createdAt ?: Date(),
            updatedAt = Date(),
            outTime = binding.outTimeEt.text.toString(),
            prepayment = binding.prepaySwitch.isChecked,
        )
    }

    private fun getUserPrepaymentObj(description: String, percentage: Double): SpaPrepayment {
        return SpaPrepayment(
            id = "",
            spaId = spaObj.id,
            description = description,
            percentage = percentage,
        )
    }

    @SuppressLint("NewApi")
    fun validateAndFormatTime(input: String): String? {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        return try {
            val formattedTime = if (input.matches(Regex("^\\d{1}:\\d{2}$"))) {
                "0$input"
            } else if (input.matches(Regex("^\\d{1,2}$"))) {
                "${input.padStart(2, '0')}:00"
            } else {
                input
            }
            val time = LocalTime.parse(formattedTime, timeFormat)
            time.format(timeFormat)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun cleanInputs() {
        binding.spaNameEt.setText("")
        binding.locationSpaBtn.setText("")
        binding.telSpaEt.setText("")
        binding.inTimeEt.setText("")
        binding.outTimeEt.setText("")
        binding.descriptionEt.setText("")
    }



    private fun showBottomSheetReportSpa(spaPrepayment: SpaPrepayment?) {
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
                        binding.prepaySwitch.isChecked = false
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            with(bottomSheetBinding) {
                // Make EditText focused when bottom sheet opens
                reportReason.requestFocus()
                titleReport.text = "Activar Prepago"
                instruction.text = "Esta aplicacion solo soporta imagenes y pdf para subir evidencia de pago."
                reportReason.hint = "Escriba metodos de pago, porcentaje de adelanto o instrucciones de pago."
                btnSubmitReport.text = "Activar prepago"
                percentagePrepayment.visibility = View.VISIBLE

                spaPrepayment?.let {
                    reportReason.setText(it.description)
                    percentagePrepayment.setText(it.percentage.toString())
                }

                btnSubmitReport.setOnClickListener {
                    val result = reportValidator.validate(reportReason.text.toString())
                    val percentagePrepaymentTxt = percentagePrepayment.text.toString()
                    if (result.isValid && percentagePrepaymentTxt.isNotEmpty()) {
                        spaPrepay = Pair(reportReason.text.toString(), percentagePrepaymentTxt)

                        dismiss()
                    } else if (percentagePrepaymentTxt.isEmpty()) {
                        toast("Agregar Porcentaje de prepago")
                    } else {
                        result.errorMessageResId?.let { id ->
                            val string = getString(id)
                            val filteredString = string
                                .replace("reporte", "prepago", ignoreCase = true)
                                .replace("Reporte", "Prepago", ignoreCase = false)
                            toast(filteredString)
                        }
                    }
                }
            }

            show()
        }
    }
}
