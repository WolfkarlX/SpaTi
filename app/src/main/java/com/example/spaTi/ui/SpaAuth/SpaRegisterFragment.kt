package com.example.spaTi.ui.SpaAuth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentSpaRegisterBinding
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.isValidEmail
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.validatePassword
import dagger.hilt.android.AndroidEntryPoint

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
            type = "2",
        )
    }

    fun validation(): Boolean {
        if (binding.spaNameLabel.text.isNullOrEmpty()){
            toast(getString(R.string.enter_first_name))
            return false
        }

        if (binding.locationSpaEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_last_name))
            return false
        }


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

        if (binding.telSpaEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_cellphone))
            return false
        } else {
            if (binding.telSpaEt.text.toString().length < 10 || binding.telSpaEt.text.toString().length > 15) {
                toast(getString(R.string.invalid_cellphone_number))
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

    override fun onStart() {
        super.onStart()
        viewModel.getSession { spa ->
            if (spa != null){
                findNavController().navigate(R.id.action_loginFragment_to_spaHomeFragment)
            }
        }
    }

}
