package com.example.spaTi.ui.Profile

import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentEditProfileBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.extractNumbersFromDate
import com.example.spaTi.util.getAge
import com.example.spaTi.util.hide
import com.example.spaTi.util.isValidEmail
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.validatePassword
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    var id = ""
    var email = ""
    var age = 0
    val TAG: String = "EditprofileFragment"
    lateinit var binding: FragmentEditProfileBinding
    val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
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

                viewModel.editUser(user = getUserObj())
            }
        }

        binding.btnCancelar.setOnClickListener {
            findNavController().navigate(R.id.action_editprofileFragment_to_myprofileFragment)
        }

    }

    fun getUserObj(): User {
        return User(
            id = id,
            email = email,
            first_name = binding.etNombre.text.toString(),
            last_name = binding.lastNameEt.text.toString(),
            cellphone = binding.telefonoEt.text.toString(),
            sex = binding.sexoEt.text.toString(),
            bornday = "" + binding.etDia.text.toString() + "/"+ binding.etMes.text.toString()+ "/" + binding.etAno.text.toString(),
            age = age.toString(),
            type = "1",
        )
    }

    fun validation(): Boolean {
        if (binding.etNombre.text.isNullOrEmpty()){
            toast(getString(R.string.enter_first_name))
            return false
        }

        if (binding.lastNameEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_last_name))
            return false
        }

        if (binding.telefonoEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_cellphone))
            return false
        } else {
            if (binding.telefonoEt.text.toString().length < 10 || binding.telefonoEt.text.toString().length > 15) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }

            val phoneNumber = binding.telefonoEt.text.toString()

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

        if (binding.sexoEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_sex))
            return false
        }

        if (binding.etDia.text.isNullOrEmpty() || binding.etMes.text.isNullOrEmpty() || binding.etAno.text.isNullOrEmpty()) {
            when {
                binding.etDia.text.isNullOrEmpty() -> toast(getString(R.string.enter_day))
                binding.etMes.text.isNullOrEmpty() -> toast(getString(R.string.enter_month))
                binding.etAno.text.isNullOrEmpty() -> toast(getString(R.string.enter_year))
            }
            return false
        }else{

            val day = binding.etDia.text.toString().toIntOrNull()
            if (day == null || day <= 0 || day > 31) {
                toast(getString(R.string.invalid_day))
                return false
            }

            // Validate month (1-12)
            val month = binding.etMes.text.toString().toIntOrNull()
            if (month == null || month <= 0 || month > 12) {
                toast(getString(R.string.invalid_month))
                return false
            }

            // Validate year (greater than 1900 and less than the current year)
            val year = binding.etAno.text.toString().toIntOrNull()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year == null || year <= 1900 || year >= currentYear) {
                toast(getString(R.string.invalid_year))
                return false
            }

            age = getAge(binding.etDia.text.toString().toInt(), binding.etMes.text.toString().toInt(), binding.etAno.text.toString().toInt())
            if (age < 18) {
                toast(getString(R.string.invalid_age))
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
                    findNavController().navigate(R.id.action_editprofileFragment_to_myprofileFragment)

                }
            }
        }
    }

    fun setData(user: User?) {
        user?.let {
            val bornday = extractNumbersFromDate(it.bornday)
            email = it.email

            id = it.id
            binding.etNombre.setText(it.first_name)
            binding.lastNameEt.setText(it.last_name)
            binding.telefonoEt.setText(it.cellphone)
            binding.sexoEt.setText(it.sex)
            binding.etDia.setText(bornday[0])
            binding.etMes.setText(bornday[1])
            binding.etAno.setText(bornday[2])

        }
    }

    fun CleanInputs() {
        binding.etNombre.setText("")
        binding.lastNameEt.setText("")
        binding.telefonoEt.setText("")
        binding.sexoEt.setText("")
        binding.etDia.setText("")
        binding.etMes.setText("")
        binding.etAno.setText("")
    }
}