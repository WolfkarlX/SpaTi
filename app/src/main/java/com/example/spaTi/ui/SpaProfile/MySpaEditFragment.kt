package com.example.spaTi.ui.SpaProfile

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentMySpaEditBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
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

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Prevent navigating back
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

    fun getUserObj(): Spa {

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
