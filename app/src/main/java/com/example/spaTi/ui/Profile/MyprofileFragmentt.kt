package com.example.spaTi.ui.Profile

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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MyprofileFragmentt : Fragment() {

    val TAG: String = "MyprofileFragmentt"
    lateinit var binding: FragmentMyProfileBinding
    val viewModel: ProfileViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var photoUri: Uri
    private val IMAGE_PICK_CODE = 1000
    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observer()
        //viewModel.getSession()
        viewModel.syncSessionWithDatabase()

        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_myprofileFragment_to_editprofileFragment)
        }

        binding.saveButton.setOnClickListener {
            viewModel.logout {
                findNavController().navigate(R.id.action_myprofileFragment_to_loginFragment)
            }
        }

        binding.changeProfilePicture.setOnClickListener {
            showImageSourceOptions()
        }

        // Al hacer clic en la imagen de perfil, abre el diálogo con la imagen en grande
        binding.profileImage.setOnClickListener {
            showProfileImageInDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        //reloadProfileImage()
    }

    private fun reloadProfileImage() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("profile_images/${userId}.jpg")

        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            Glide.with(requireContext())
                .load(downloadUrl.toString())
                .skipMemoryCache(true) // Evita que Glide use la imagen en caché
                .into(binding.profileImage)
        }.addOnFailureListener {
            toast("No se pudo cargar la imagen actualizada.")
        }
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Tomar foto", "Seleccionar desde galería")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar una opción")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> pickImageFromGallery()
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

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startCrop(photoUri)
        } else {
            toast("No se tomó ninguna foto.")
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val sourceUri = result.data?.data
            sourceUri?.let {
                startCrop(it)
            }
        }
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
            uploadProfilePicture(resultUri)
        }
    }

    private fun uploadProfilePicture(imageUri: Uri?) {
        if (imageUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_images/${userId}.jpg")

            binding.sessionProgress.show()
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        viewModel.updateProfilePicture(downloadUrl.toString(), userId)
                        binding.sessionProgress.hide()
                        toast("Foto de perfil actualizada exitosamente")
                        Glide.with(requireContext())
                            .load(downloadUrl.toString())
                            .into(binding.profileImage)
                    }
                }
                .addOnFailureListener {
                    binding.sessionProgress.hide()
                    toast("Error al subir la imagen.")
                }
        }
    }

    private fun showProfileImageInDialog() {
        val imageUrl = binding.profileImage.tag?.toString() ?: ""
        if (imageUrl.isNotEmpty()) {
            ImageDialogFragment.newInstance(imageUrl).show(parentFragmentManager, "ImageDialog")
        } else {
            toast("No se pudo cargar la imagen.")
        }
    }

    // Observe the session LiveData from the ViewModel
    private fun observer() {
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
                    viewModel.logout{
                        findNavController().navigate(R.id.action_myprofileFragment_to_loginFragment)
                    }
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.sessionProgress.hide()
                    state.data?.let {
                        userId = it.id // Initialize userId here
                        setData(it) // Call setData to update UI with user info
                        reloadProfileImage() // Reload the profile image after userId is set
                    }
                }
            }
        }
    }

    // Update the UI with user session data
    fun setData(user: User?) {
        user?.let {
            if(user.status=="active") {
                binding.firstName.setText(it.first_name)
                binding.lastNames.setText(it.last_name)
                binding.email.setText(it.email)
                binding.phoneNumber.setText(it.cellphone)
                binding.bornday.setText(it.bornday)
                binding.sex.setText(it.sex)
            }else{
                viewModel.logout{
                    toast("User not available")
                    findNavController().navigate(R.id.action_myprofileFragment_to_loginFragment)
                }
            }

            // Verificar que profileImageUrl no esté vacío o nulo
            if (it.profileImageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(it.profileImageUrl)
                    .into(binding.profileImage)
                binding.profileImage.tag = it.profileImageUrl // Guardamos la URL de la imagen
            } else {
                // Si no hay URL de la imagen de perfil, mostrar una imagen predeterminada
                Glide.with(requireContext())
                    .load(R.drawable.user_def) // Asegúrate de tener una imagen predeterminada
                    .into(binding.profileImage)
                binding.profileImage.tag = "" // Limpiamos la URL
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
                toast("Permiso de cámara denegado. No puedes tomar una foto.")
            }
        }
    }
}
