package com.example.spaTi.ui.Profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.spaTi.R
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentMyProfileBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.storage.FirebaseStorage

@AndroidEntryPoint
class MyProfileFragment : Fragment() {

    val TAG: String = "MyProfileFragment"
    lateinit var binding: FragmentMyProfileBinding
    val viewModel: ProfileViewModel by viewModels()
    private lateinit var userId: String
    private val IMAGE_PICK_CODE = 1000 // Código para seleccionar la imagen

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llamar a observer para observar el estado de la sesión
        observer()
        viewModel.getSession()

        // Edit button navigation
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_myprofileFragment_to_editprofileFragment)
        }

        binding.saveButton.setOnClickListener {
            viewModel.logout {
                findNavController().navigate(R.id.action_myprofileFragment_to_loginFragment)
            }
        }

        // Configura el click listener para cambiar la foto de perfil
        binding.changeProfilePicture.setOnClickListener {
            pickImageFromGallery()
        }
    }

    // Método para seleccionar una imagen de la galería
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Manejo del resultado de la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            uploadProfilePicture(imageUri) // Llama a la función para subir la imagen
        }
    }

    // Método para subir la imagen a Firebase Storage
    private fun uploadProfilePicture(imageUri: Uri?) {
        if (imageUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_images/${userId}.jpg") // Ajusta la ruta si es necesario

            // Muestra el progreso mientras se sube la imagen
            binding.sessionProgress.show()
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    // Obtén la URL de descarga
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        viewModel.updateProfilePicture(downloadUrl.toString(), userId) // Llama a updateProfilePicture con URL y userId
                        binding.sessionProgress.hide()
                        toast("Foto de perfil actualizada exitosamente")

                        // Cargar la nueva imagen en el ImageView
                        Glide.with(requireContext())
                            .load(downloadUrl.toString())
                            .into(binding.profileImage) // Reemplaza con el ID correcto de tu ImageView
                    }
                }
                .addOnFailureListener {
                    binding.sessionProgress.hide()
                    toast("Error al subir la imagen.")
                }
        }
    }

    // Observa el LiveData de sesión del ViewModel
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
                    userId = state.data?.id ?: "" // Guarda el userId
                    setData(state.data) // Llama a setData para actualizar la UI con la info del usuario
                }
            }
        }
    }

    // Actualiza la UI con los datos de la sesión del usuario
    private fun setData(user: User?) {
        user?.let {
            binding.firstName.setText(it.first_name)
            binding.lastNames.setText(it.last_name)
            binding.email.setText(it.email)
            binding.phoneNumber.setText(it.cellphone)
            binding.bornday.setText(it.bornday)
            binding.sex.setText(it.sex)

            // Cargar la imagen de perfil si está disponible
            Glide.with(requireContext())
                .load(it.profileImageUrl) // Asegúrate de que 'profileImageUrl' sea el nombre del campo
                .into(binding.profileImage) // Reemplaza con el ID correcto de tu ImageView
        }
    }
}
