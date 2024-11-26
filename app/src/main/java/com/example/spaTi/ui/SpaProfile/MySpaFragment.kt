package com.example.spaTi.ui.SpaProfile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentMySpaBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MySpaFragment : Fragment() {

    val TAG: String = "MySpaFragment"
    val viewModel: MySpaViewModel by viewModels()

    private var _binding: FragmentMySpaBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private lateinit var photoUri: Uri
    private val IMAGE_PICK_CODE = 1000
    private val REQUEST_CAMERA_PERMISSION = 1001

    // Launcher for selecting images from the gallery
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val sourceUri = result.data?.data
            sourceUri?.let {
                selectedImageUri = it
                binding.profileImage.setImageURI(it)
                viewModel.updateProfileImage("spaId", it)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startCrop(photoUri)
        } else {
            toast("No se tomó ninguna foto.")
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
            showImageSourceOptions()
        }
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Tomar foto", "Seleccionar desde galería")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar una opción")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun openCamera() {
        val photoFile = File(requireContext().cacheDir, "photo.jpg")
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped.jpg"))
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK && data != null) {
            val resultUri = UCrop.getOutput(data)
            resultUri?.let {
                viewModel.updateProfileImage("spaId", it)
                binding.profileImage.setImageURI(it)
            }
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
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    setData(state.data)
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
            binding.prepaySpa.setText(if (it.prepayment) "Activo" else "Inactivo")

            if (!it.profileImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                toast("Permiso denegado para acceder a la cámara.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
