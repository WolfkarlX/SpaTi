package com.example.spaTi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentRegisterBinding
import com.example.spaTi.util.*
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    var age = 0
    val TAG: String = "RegisterFragment"
    lateinit var binding: FragmentRegisterBinding
    val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val opcionesSexo = arrayOf("Hombre", "Mujer")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, opcionesSexo)
        binding.sexoEt.setAdapter(adapter)

        binding.sexoEt.setOnClickListener {
            binding.sexoEt.showDropDown()
        }

        observer()

        binding.termsTextView.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_TermsFragment)
        }

        binding.registerBtn.setOnClickListener {
            if (validation()){
                viewModel.register(
                    email = binding.emailEt.text.toString(),
                    password = binding.passEt.text.toString(),
                    user = getUserObj(),
                )
            }
        }
    }

    fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.registerBtn.text = ""
                    binding.registerProgress.show()
                }
                is UiState.Failure -> {
                    binding.registerBtn.text = "Register"
                    binding.registerProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.registerBtn.text = "Register"
                    binding.registerProgress.hide()
                    toast(state.data)
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
            }
        }
    }

    fun getUserObj(): User {
        return User(
            id = "",
            first_name = binding.firstNameEt.text.toString(),
            last_name = binding.lastNameEt.text.toString(),
            email = binding.emailEt.text.toString(),
            cellphone = binding.telefonoEt.text.toString(),
            sex = binding.sexoEt.text.toString(),
            bornday = "${binding.diaEt.text}/${binding.mesEt.text}/${binding.anoEt.text}",
            age = age.toString(),
            reports = "0",
            type = "1",
            status = "active"
        )
    }

    fun validation(): Boolean {
        if (binding.firstNameEt.text.isNullOrEmpty()){
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

        if (binding.diaEt.text.isNullOrEmpty() || binding.mesEt.text.isNullOrEmpty() || binding.anoEt.text.isNullOrEmpty()) {
            when {
                binding.diaEt.text.isNullOrEmpty() -> toast(getString(R.string.enter_day))
                binding.mesEt.text.isNullOrEmpty() -> toast(getString(R.string.enter_month))
                binding.anoEt.text.isNullOrEmpty() -> toast(getString(R.string.enter_year))
            }
            return false
        }else{

            val day = binding.diaEt.text.toString().toIntOrNull()
            if (day == null || day <= 0 || day > 31) {
                toast(getString(R.string.invalid_day))
                return false
            }

            // Validate month (1-12)
            val month = binding.mesEt.text.toString().toIntOrNull()
            if (month == null || month <= 0 || month > 12) {
                toast(getString(R.string.invalid_month))
                return false
            }

            // Validate year (greater than 1900 and less than the current year)
            val year = binding.anoEt.text.toString().toIntOrNull()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year == null || year <= 1900 || year >= currentYear) {
                toast(getString(R.string.invalid_year))
                return false
            }

            age = getAge(binding.diaEt.text.toString().toInt(), binding.mesEt.text.toString().toInt(), binding.anoEt.text.toString().toInt())
            if (age < 18) {
                toast(getString(R.string.invalid_age))
                return false
            }
        }

        if (binding.emailEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_email))
            return false
        }else{
            if (!binding.emailEt.text.toString().isValidEmail()){
                toast(getString(R.string.invalid_email))
                return false
            }
        }
        if (binding.passEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_password))
            return false
        }else{
            val (isValid, message) = validatePassword(requireContext(), binding.passEt.text.toString())

            if(!isValid){
                toast(message)
                return false
            }
        }

        if (!binding.termsCheckbox.isChecked) {
            toast(getString(R.string.check_terms))
            return false
        }

        return true
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSession { user ->
            if (user != null){
                findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
            }
        }
    }
}