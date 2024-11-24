package com.example.spaTi.ui.SpaAuth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentSpaRegisterBinding
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.isValidEmail
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.validatePassword
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@AndroidEntryPoint
class SpaRegisterFragment : Fragment() {
    val TAG: String = "SpaRegisterFragment"
    lateinit var binding: FragmentSpaRegisterBinding
    val viewModel: SpaAuthViewModel by viewModels()

    // Variables para almacenar la ubicación seleccionada
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    // Variable para controlar la visibilidad de la contraseña
    private var isPasswordVisible = false

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001  // Definición de la constante
    }

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

        // Configuración del listener para el botón de visibilidad de la contraseña
        binding.passSpaEt.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Detectar si el click fue en el icono de visibilidad
                if (event.rawX >= (binding.passSpaEt.right - binding.passSpaEt.compoundDrawables[2].bounds.width())) {
                    // Solo cambiar la visibilidad si hay texto en el EditText
                    if (!binding.passSpaEt.text.isNullOrEmpty()) {
                        togglePasswordVisibility()
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

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

        binding.locationSpaBtn.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivityForResult(intent, LOCATION_REQUEST_CODE)
        }
    }

    // Función para alternar la visibilidad de la contraseña
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Cambiar a contraseña oculta usando TransformationMethod
            binding.passSpaEt.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            binding.passSpaEt.setCompoundDrawablesWithIntrinsicBounds(
                null, null, resources.getDrawable(R.drawable.no_show_password, null), null
            )
        } else {
            // Cambiar a contraseña visible usando TransformationMethod
            binding.passSpaEt.transformationMethod = null
            binding.passSpaEt.setCompoundDrawablesWithIntrinsicBounds(
                null, null, resources.getDrawable(R.drawable.show_password, null), null
            )
        }

        // Mantener la visibilidad de la contraseña
        isPasswordVisible = !isPasswordVisible

        // Restablecer la tipografía para que no se pierda
        binding.passSpaEt.setTypeface(binding.passSpaEt.typeface)

        // Mantener el cursor en la posición correcta
        binding.passSpaEt.setSelection(binding.passSpaEt.text?.length ?: 0)
    }

    // Observar los estados del registro
    fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.registerBtn.setText("")
                    binding.registerProgress.show()
                }
                is UiState.Failure -> {
                    binding.registerBtn.setText("Register")
                    binding.registerProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.registerBtn.setText("Register")
                    binding.registerProgress.hide()
                    toast(state.data)
                    findNavController().navigate(R.id.action_registerSpaFragment_to_loginFragment)
                }
            }
        }
    }

    // Crear el objeto Spa para el registro
    fun getSpaObj(): Spa {
        return Spa(
            id = "",
            spa_name = binding.spaNameEt.text.toString(),
            location = selectedAddress ?: "",
            coordinates = selectedLatLng?.let { "Lat: ${it.latitude}, Lon: ${it.longitude}" } ?: "",
            email = binding.emailSpaEt.text.toString(),
            cellphone = binding.telSpaEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            inTime = binding.inTimeEt.text.toString(),
            outTime = binding.outTimeEt.text.toString(),
            reports = "0",
            type = "2",
        )
    }

    // Función para validar los datos del formulario
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
            val descriptionRegex = Regex("^[a-zA-Z0-9.,!?'\"\\- ]{20,500}$")

            if (!description.matches(descriptionRegex)) {
                toast(getString(R.string.invalid_description_too_long))
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

    override fun onStart() {
        super.onStart()
        viewModel.getSession { spa ->
            if (spa != null){
                findNavController().navigate(R.id.action_loginFragment_to_spaHomeFragment)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedLocation = data?.getParcelableExtra<LatLng>("selectedLocation")
            val selectedAddress = data?.getStringExtra("selectedAddress")

            selectedLatLng = selectedLocation
            this.selectedAddress = selectedAddress

            binding.locationSpaBtn.text = selectedAddress ?: getString(R.string.location_not_selected)
        }
    }
}
