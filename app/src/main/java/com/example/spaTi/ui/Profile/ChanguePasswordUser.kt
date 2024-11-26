package com.example.spaTi.ui.Profile

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spaTi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider

class ChanguePasswordUser : Fragment(R.layout.fragment_changue_password_user) {

    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar las vistas
        oldPasswordEditText = view.findViewById(R.id.oldPassword)
        newPasswordEditText = view.findViewById(R.id.newPassword)
        confirmPasswordEditText = view.findViewById(R.id.confirmPassword)
        changePasswordButton = view.findViewById(R.id.btnChangePassword)

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configurar el botón de cambio de contraseña
        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Validar campos vacíos
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                // Validar si las contraseñas coinciden
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else if (!isValidPassword(newPassword)) {
                // Validar si la nueva contraseña es válida
                Toast.makeText(requireContext(), "La contraseña debe tener al menos un carácter especial y no puede contener números consecutivos.", Toast.LENGTH_LONG).show()
            } else {
                // Cambiar la contraseña
                changePassword(oldPassword, newPassword)
            }
        }

        // Configurar la visibilidad de las contraseñas
        oldPasswordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = oldPasswordEditText.compoundDrawables[2]
                if (event.rawX >= (oldPasswordEditText.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility(oldPasswordEditText, isOldPasswordVisible)
                    isOldPasswordVisible = !isOldPasswordVisible
                    return@setOnTouchListener true
                }
            }
            false
        }

        newPasswordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = newPasswordEditText.compoundDrawables[2]
                if (event.rawX >= (newPasswordEditText.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility(newPasswordEditText, isNewPasswordVisible)
                    isNewPasswordVisible = !isNewPasswordVisible
                    return@setOnTouchListener true
                }
            }
            false
        }

        confirmPasswordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = confirmPasswordEditText.compoundDrawables[2]
                if (event.rawX >= (confirmPasswordEditText.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility(confirmPasswordEditText, isConfirmPasswordVisible)
                    isConfirmPasswordVisible = !isConfirmPasswordVisible
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    // Función para cambiar la visibilidad de la contraseña
    private fun togglePasswordVisibility(editText: EditText, isPasswordVisible: Boolean) {
        if (isPasswordVisible) {
            // Cambiar a contraseña oculta usando TransformationMethod
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            editText.setCompoundDrawablesWithIntrinsicBounds(
                null, null, resources.getDrawable(R.drawable.no_show_password, null), null
            )
        } else {
            // Cambiar a contraseña visible usando TransformationMethod
            editText.transformationMethod = null
            editText.setCompoundDrawablesWithIntrinsicBounds(
                null, null, resources.getDrawable(R.drawable.show_password, null), null
            )
        }
    }

    // Función para cambiar la contraseña
    private fun changePassword(oldPassword: String, newPassword: String) {
        val user = firebaseAuth.currentUser

        // Crear las credenciales para la reautenticación
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)

        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Reautenticación exitosa, cambiar la contraseña
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Contraseña cambiada exitosamente, mantener al usuario en el mismo fragmento
                        Toast.makeText(requireContext(), "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        // Error al cambiar la contraseña
                        Toast.makeText(requireContext(), "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Error de reautenticación
                Toast.makeText(requireContext(), "Error de autenticación. Verifica la contraseña actual.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para validar la nueva contraseña
    private fun isValidPassword(password: String): Boolean {
        // Validar que la contraseña contiene al menos un carácter especial
        val hasSpecialChar = password.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]"))
        // Validar que la contraseña no contenga números consecutivos (por ejemplo: 123, 234, etc.)
        val hasConsecutiveNumbers = Regex(".*(\\d)\\1\\1.*").containsMatchIn(password)

        return hasSpecialChar && !hasConsecutiveNumbers
    }
}
