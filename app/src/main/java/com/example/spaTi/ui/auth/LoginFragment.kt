package com.example.spaTi.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentLoginBinding
import com.example.spaTi.databinding.FragmentRegisterBinding
import com.example.spaTi.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    var UserType: Int = 0
    val TAG: String = "RegisterFragment"
    lateinit var binding: FragmentLoginBinding
    val viewModelUser: AuthViewModel by viewModels()
    val viewModelSpa: SpaAuthViewModel by viewModels()

    private var isPasswordVisible = false

    override fun onCreateView(
        inflaterf: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up observers once
        setupObservers()

        // Configurar visibilidad de la contraseña
        binding.passEt.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Detectar si el click fue en el icono de visibilidad
                if (event.rawX >= (binding.passEt.right - binding.passEt.compoundDrawables[2].bounds.width())) {
                    // Solo cambiar la visibilidad si hay texto en el EditText
                    if (!binding.passEt.text.isNullOrEmpty()) {
                        togglePasswordVisibility()
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.loginBtn.setOnClickListener {
            if (validation()) {
                attemptSpaLogin(
                    email = binding.emailEt.text.toString(),
                    password = binding.passEt.text.toString(),
                )
            }
        }

        // Register the navigation handlers
        binding.forgotPassLabel.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.registerLabelSpa.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_SpaRegisterFragment)
        }

        binding.registerLabel.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Cambiar a contraseña oculta usando TransformationMethod
            binding.passEt.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            binding.passEt.setCompoundDrawablesWithIntrinsicBounds(
                null, null, resources.getDrawable(R.drawable.no_show_password, null), null
            )
        } else {
            // Cambiar a contraseña visible usando TransformationMethod
            binding.passEt.transformationMethod = null
            binding.passEt.setCompoundDrawablesWithIntrinsicBounds(
                null, null, resources.getDrawable(R.drawable.show_password, null), null
            )
        }

        // Mantener la visibilidad de la contraseña
        isPasswordVisible = !isPasswordVisible

        // Restablecer la tipografía para que no se pierda
        binding.passEt.setTypeface(binding.passEt.typeface)

        // Mantener el cursor en la posición correcta
        binding.passEt.setSelection(binding.passEt.text?.length ?: 0)
    }


    // Set up observers once
    private fun setupObservers() {
        // Observer for Spa login
        viewModelSpa.login.observe(viewLifecycleOwner) { state ->
            handleSpaLoginState(state)
        }

        // Observer for User login
        viewModelUser.login.observe(viewLifecycleOwner) { state ->
            handleUserLoginState(state)
        }
    }

    private fun attemptSpaLogin(email: String, password: String) {
        // Attempt spa login
        viewModelSpa.login(email = email, password = password)
    }

    private fun attemptUserLogin(email: String, password: String) {
        // Attempt user login
        viewModelUser.login(email = email.toString(), password = password.toString())
    }

    private fun handleSpaLoginState(state: UiState<String>) {
        when (state) {
            is UiState.Loading -> {
                binding.loginBtn.text = ""
                binding.loginProgress.show()
            }
            is UiState.Failure -> {
                // Spa login failed, now attempt user login
                attemptUserLogin(binding.emailEt.text.toString(), binding.passEt.text.toString())
            }
            is UiState.Success -> {
                // Spa login successful
                binding.loginBtn.text = "Login"
                binding.loginProgress.hide()
                toast(state.data)
                if (state.data == "Login successfully!") {
                    findNavController().navigate(R.id.action_loginFragment_to_spaHomeFragment)
                }
            }
        }
    }

    private fun handleUserLoginState(state: UiState<String>) {
        when (state) {
            is UiState.Loading -> {
                binding.loginBtn.text = ""
                binding.loginProgress.show()
            }
            is UiState.Failure -> {
                // Both logins failed
                binding.loginBtn.text = "Login"
                binding.loginProgress.hide()
                toast(state.error)
            }
            is UiState.Success -> {
                // User login successful
                binding.loginBtn.text = "Login"
                binding.loginProgress.hide()
                toast(state.data)
                findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
            }
        }
    }

    fun validation(): Boolean {
        var isValid = true

        if (binding.emailEt.text.isNullOrEmpty()) {
            isValid = false
            toast(getString(R.string.enter_email))
        } else {
            if (!binding.emailEt.text.toString().isValidEmail()) {
                isValid = false
                toast(getString(R.string.invalid_email))
            }
        }
        if (binding.passEt.text.isNullOrEmpty()) {
            isValid = false
            toast(getString(R.string.enter_password))
        } else {
            if (binding.passEt.text.toString().length < 8) {
                isValid = false
                toast(getString(R.string.short_password))
            }
        }
        return isValid
    }

    override fun onStart() {
        super.onStart()
        checkSessions()
    }

    private fun checkSessions() {
        // Check for spa session first
        viewModelSpa.getSession { spa ->
            if (spa != null && spa.type == "2") {
                // Navigate to Spa services if session exists and type is "2"
                findNavController().navigate(R.id.action_loginFragment_to_spaHomeFragment)
            } else {
                // If no valid spa session, check for user session
                viewModelUser.getSession { user ->
                    if (user != null && user.type == "1" && user.status == "active") {
                        // Navigate to user notes if session exists and type is "1" and is active as status
                        findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
                    }
                }
            }
        }
    }
}