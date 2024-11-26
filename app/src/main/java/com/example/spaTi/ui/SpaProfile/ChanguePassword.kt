package com.example.spaTi.ui.SpaProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentChanguePasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import androidx.core.content.ContextCompat

class ChangePasswordFragment : Fragment(R.layout.fragment_changue_password) {

    private lateinit var binding: FragmentChanguePasswordBinding
    private lateinit var auth: FirebaseAuth
    private var oldPasswordVisible = false
    private var newPasswordVisible = false
    private var confirmPasswordVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()
        binding = FragmentChanguePasswordBinding.bind(view)

        // Configura el listener para el botón Cambiar Contraseña
        binding.btnChangePassword.setOnClickListener {
            val oldPassword = binding.oldPassword.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            // Validación de los campos de texto
            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar que la nueva contraseña tenga al menos un carácter especial
            if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*".toRegex())) {
                Toast.makeText(requireContext(), "La nueva contraseña debe contener al menos un carácter especial", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar si las contraseñas coinciden
            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reautenticar al usuario con la contraseña antigua
            val user = auth.currentUser
            if (user != null) {
                val credentials = EmailAuthProvider.getCredential(user.email!!, oldPassword)

                user.reauthenticate(credentials).addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // Si la reautenticación es exitosa, cambiar la contraseña
                        user.updatePassword(newPassword).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Contraseña cambiada con éxito", Toast.LENGTH_SHORT).show()
                                findNavController().navigateUp()  // Navegar hacia atrás al fragmento anterior
                            } else {
                                Toast.makeText(requireContext(), "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Reautenticación fallida. Verifica la contraseña antigua", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Toggle visibility of old password
        binding.oldPassword.setOnTouchListener { v, event ->
            val drawableRight = binding.oldPassword.compoundDrawables[2]
            if (drawableRight != null) {
                if (event.x >= binding.oldPassword.right - drawableRight.bounds.width()) {
                    oldPasswordVisible = !oldPasswordVisible
                    togglePasswordVisibility(binding.oldPassword, oldPasswordVisible)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Toggle visibility of new password
        binding.newPassword.setOnTouchListener { v, event ->
            val drawableRight = binding.newPassword.compoundDrawables[2]
            if (drawableRight != null) {
                if (event.x >= binding.newPassword.right - drawableRight.bounds.width()) {
                    newPasswordVisible = !newPasswordVisible
                    togglePasswordVisibility(binding.newPassword, newPasswordVisible)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Toggle visibility of confirm password
        binding.confirmPassword.setOnTouchListener { v, event ->
            val drawableRight = binding.confirmPassword.compoundDrawables[2]
            if (drawableRight != null) {
                if (event.x >= binding.confirmPassword.right - drawableRight.bounds.width()) {
                    confirmPasswordVisible = !confirmPasswordVisible
                    togglePasswordVisibility(binding.confirmPassword, confirmPasswordVisible)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isPasswordVisible: Boolean) {
        if (isPasswordVisible) {
            // Mostrar la contraseña
            editText.transformationMethod = null
            val showPasswordDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.show_password)
            editText.setCompoundDrawablesWithIntrinsicBounds(
                null, null, showPasswordDrawable, null
            )
        } else {
            // Ocultar la contraseña
            editText.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            val hidePasswordDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.no_show_password)
            editText.setCompoundDrawablesWithIntrinsicBounds(
                null, null, hidePasswordDrawable, null
            )
        }

        // Mantener el cursor en la posición correcta
        editText.setSelection(editText.text?.length ?: 0)
    }
}
