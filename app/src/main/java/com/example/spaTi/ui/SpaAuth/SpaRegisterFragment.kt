package com.example.spaTi.ui.SpaAuth

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentSpaRegisterBinding
//import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.isValidEmail
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.validatePassword
import com.google.protobuf.Internal.BooleanList
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException



@AndroidEntryPoint
class SpaRegisterFragment : Fragment() {
    val TAG: String = "SpaRegisterFragment"
    lateinit var binding: FragmentSpaRegisterBinding
    val viewModel: SpaAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpaRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        binding.termsTextView.setOnClickListener {
            findNavController().navigate(R.id.action_registerSpaFragment_to_TermsFragment)
        }
        binding.registerBtn.setOnClickListener {
            if (validation()){
                viewModel.registerSpa(
                    email = binding.emailSpaEt.text.toString(),
                    password = binding.passSpaEt.text.toString(),
                    spa = getSpaObj(),
                )
            }
        }
    }

    fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    Log.d("SpaRegisterFragment observer", "LOADING")
                    binding.registerBtn.setText("")
                    binding.registerProgress.show()
                }
                is UiState.Failure -> {
                    Log.d("SpaRegisterFragment observer", "FAILURE")
                    binding.registerBtn.setText("Register")
                    binding.registerProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    Log.d("SpaRegisterFragment observer", "SUCCESS")
                    binding.registerBtn.setText("Register")
                    binding.registerProgress.hide()
                    toast(state.data)
                    findNavController().navigate(R.id.action_registerSpaFragment_to_loginFragment)
                }
            }
        }
    }

    fun getSpaObj(): Spa {
        return Spa(
            id = "",
            spa_name = binding.spaNameEt.text.toString(),
            location = binding.locationSpaEt.text.toString(),
            email = binding.emailSpaEt.text.toString(),
            cellphone = binding.telSpaEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            inTime = binding.inTimeEt.text.toString(),
            outTime = binding.outTimeEt.text.toString(),
            reports = "0",
            type = "2",
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validation(): Boolean {

        if (binding.emailSpaLabel.text.isNullOrEmpty()){
            toast(getString(R.string.enter_email))
            return false
        } else {
            if (!binding.emailSpaEt.text.toString().isValidEmail()){
                toast(getString(R.string.invalid_email))
                return false
            }
        }

        if (binding.passSpaEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_password))
            return false
        } else {
            val (isValid, message) = validatePassword(requireContext(), binding.passSpaEt.text.toString())

            if(!isValid){
                toast(message)
                return false
            }

        }

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
                Log.d("XDDD", validTime) // Output: 12:00
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

        if (!binding.termsCheckbox.isChecked) {
            toast(getString(R.string.check_terms))
            return false
        }

        return true
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

    override fun onStart() {
        super.onStart()
        viewModel.getSession { spa ->
            if (spa != null){
                findNavController().navigate(R.id.action_loginFragment_to_spaHomeFragment)
            }
        }
    }

}
