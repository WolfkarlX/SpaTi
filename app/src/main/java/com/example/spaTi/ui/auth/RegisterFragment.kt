package com.example.spaTi.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentRegisterBinding
import com.example.spaTi.util.*
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.util.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    var age = 0
    val TAG: String = "RegisterFragment"
    lateinit var binding:  FragmentRegisterBinding
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
        observer()
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
                    findNavController().navigate(R.id.action_registerFragment_to_noteListingFragment)
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
            bornday = "" + binding.diaEt.text.toString() + "/"+ binding.mesEt.text.toString()+ "/" + binding.anoEt.text.toString(),
            age = age.toString(),
            type = "1",
        )
    }

    fun validation(): Boolean {
        var isValid = true

        if (binding.firstNameEt.text.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_first_name))
        }

        if (binding.lastNameEt.text.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_last_name))
        }

        if (binding.telefonoEt.text.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_cellphone))
        }else{
            if (binding.telefonoEt.text.toString().length < 10 || binding.telefonoEt.text.toString().length > 15) {
                isValid = false
                toast(getString(R.string.invalid_cellphone_number))
            }
        }

        if (binding.sexoEt.text.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_sex))
        }

        if (binding.diaEt.text.isNullOrEmpty() || binding.mesEt.text.isNullOrEmpty() || binding.anoEt.text.isNullOrEmpty()) {
            isValid = false
            when {
                binding.diaEt.text.isNullOrEmpty() -> toast(getString(R.string.enter_day))
                binding.mesEt.text.isNullOrEmpty() -> toast(getString(R.string.enter_month))
                binding.anoEt.text.isNullOrEmpty() -> toast(getString(R.string.enter_year))
            }
        }else{
            age = getAge(binding.diaEt.text.toString().toInt(), binding.mesEt.text.toString().toInt(), binding.anoEt.text.toString().toInt())
            if (age < 18) {
                isValid = false
                toast(getString(R.string.invalid_age))
            }
        }

        if (binding.emailEt.text.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_email))
        }else{
            if (!binding.emailEt.text.toString().isValidEmail()){
                isValid = false
                toast(getString(R.string.invalid_email))
            }
        }
        if (binding.passEt.text.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_password))
        }else{
            if (binding.passEt.text.toString().length < 8){
                isValid = false
                toast(getString(R.string.invalid_password))
            }
        }
        return isValid
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSession { user ->
            if (user != null){
                Log.d("XDDD", user.toString())
                findNavController().navigate(R.id.action_loginFragment_to_noteListingFragment)
            }
        }
    }

}