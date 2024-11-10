package com.example.spaTi.ui.SpaProfile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentMySpaBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySpaFragment : Fragment() {
    val TAG: String = "MySpaFragment"
    val viewModel: MySpaViewModel by viewModels()

    private var _binding: FragmentMySpaBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImage.setImageURI(it)
            // Subir la imagen seleccionada
            viewModel.updateProfileImage("spaId", it) // Usa el ID real del spa en lugar de "spaId"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMySpaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent back navigation
            }
        })

        observer()
        viewModel.syncSessionWithDatabase()

        binding.home.setOnClickListener {
            findNavController().navigate(R.id.action_myspaFragment_to_myaccountspaFragment)
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_myspaFragment_to_myspaeditFragment)
        }

        binding.changeProfilePhotoText.setOnClickListener {
            // Abre el selector de imágenes
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast(state.error)
                    viewModel.logout {
                        findNavController().navigate(R.id.action_myspaFragment_to_loginFragment)
                    }
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    setData(state.data)
                }
            }
        }

        viewModel.updateProfileImage.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    toast("Imagen de perfil actualizada correctamente")
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast("Error al actualizar la imagen de perfil: ${state.error}")
                }
            }
        }
    }

    private fun setData(spa: Spa?) {
        spa?.let {
            binding.nameSpa.setText(it.spa_name)
            binding.locationSpa.setText(it.location)
            binding.phoneSpa.setText(it.cellphone)
            binding.apertura.setText(it.inTime)
            binding.cierre.setText(it.outTime)
            binding.descriptionSpa.setText(it.description)

            // Cargar la URL de la imagen de perfil en el ImageView usando Glide
            if (!it.profileImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .circleCrop() // Corta la imagen en forma circular
                    .into(binding.profileImage)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
