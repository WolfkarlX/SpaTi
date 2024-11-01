package com.example.spaTi.ui.SpaProfile

import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentMySpaEditBinding
import com.example.spaTi.ui.Profile.ProfileViewModel
import com.example.spaTi.ui.spa.SpaViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.extractNumbersFromDate
import com.example.spaTi.util.getAge
import com.example.spaTi.util.hide
import com.example.spaTi.util.isValidEmail
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.validatePassword
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date

@AndroidEntryPoint
class MySpaEditFragment : Fragment() {

    var id = ""
    var email = ""
    val TAG: String = "MySpaEditFragment"
    var createdAt: Date? = null
    val viewModel: MySpaViewModel by viewModels()

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
                //Do nothing
            }
        })

        // Call observer method to observe session state
        observer()
        viewModel.getSession()

        // Edit button navigation
        binding.btnGuardar.setOnClickListener {
            if(validation()){
                observeEdit()

                val spa = getUserObj()
                viewModel.editUser(spa = getUserObj())
            }
        }

        binding.btnCancelar.setOnClickListener {
            findNavController().navigate(R.id.action_myspaeditFragment_to_myspaFragment)
        }

    }

    fun getUserObj(): Spa {
        return Spa(
            id = id,
            spa_name = binding.spaNameEt.text.toString(),
            location = binding.locationSpaEt.text.toString(),
            email = email,
            cellphone = binding.telSpaEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            type = "2",
            inTime = binding.inTimeEt.text.toString(),
            createdAt = createdAt ?: Date(),
            updatedAt = Date(), // Set the new updatedAt for the current update
            outTime = binding.outTimeEt.text.toString(),
        )
    }

    fun validation(): Boolean {

        if (binding.spaNameEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_spaname))
            return false
        }

        if (binding.locationSpaEt.text.isNullOrEmpty()){
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
    fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress or loading indicator
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    // Hide progress and show error message
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.sessionProgress.hide()
                    setData(state.data) // Call setData to update UI with user info
                }
            }
        }
    }

    fun observeEdit() {
        viewModel.editUser.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress or loading indicator
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    // Hide progress and show error message
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.sessionProgress.hide()
                    toast(state.data)
                    CleanInputs()
                    findNavController().navigate(R.id.action_myspaeditFragment_to_myspaFragment)

                }
            }
        }
    }

    fun setData(user: Spa?) {
        user?.let {
            email = it.email
            id = it.id

            createdAt = it.createdAt // Store createdAt for future updates
            Log.d("DEBUG", "Current createdAt: $createdAt")


            binding.spaNameEt.setText(it.spa_name)
            binding.locationSpaEt.setText(it.location)
            binding.telSpaEt.setText(it.cellphone)
            binding.inTimeEt.setText(it.inTime)
            binding.outTimeEt.setText(it.outTime)
            binding.descriptionEt.setText(it.description)
        }
    }

    @SuppressLint("NewApi")
    fun validateAndFormatTime(input: String): String? {
        // Formatter for HH:mm format with zero-padded hours
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

        return try {
            // Check if the input is in "H:mm" format (single-digit hour)
            val formattedTime = if (input.matches(Regex("^\\d{1}:\\d{2}$"))) {
                "0$input" // Add a leading zero to single-digit hours
            } else if (input.matches(Regex("^\\d{1,2}$"))) {
                // If input is only hours (like "12"), add ":00" for minutes
                "${input.padStart(2, '0')}:00"
            } else {
                input
            }

            // Parse and format the time to ensure zero-padding in "HH:mm" format
            val time = LocalTime.parse(formattedTime, timeFormat)
            time.format(timeFormat) // Return formatted time as "HH:mm"
        } catch (e: DateTimeParseException) {
            null // Return null if the input is invalid
        }
    }

    fun CleanInputs() {
        binding.spaNameEt.setText("")
        binding.locationSpaEt.setText("")
        binding.telSpaEt.setText("")
        binding.inTimeEt.setText("")
        binding.outTimeEt.setText("")
        binding.descriptionEt.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}