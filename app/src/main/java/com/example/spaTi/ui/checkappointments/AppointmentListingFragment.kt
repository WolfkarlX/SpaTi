package com.example.spaTi.ui.checkappointments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spaTi.R
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentAppointmentListingBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.ui.homeUser.ImageViewerUtil
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


@AndroidEntryPoint
class AppointmentListingFragment : Fragment() {

    var position = -1
    val TAG: String = "AppointmentListinFragment"
    lateinit var binding: FragmentAppointmentListingBinding
    val viewModel: AppointmentViewModel by viewModels()
    val authViewModel: SpaAuthViewModel by viewModels()
    val adapter by lazy {
        AppointmentListingAdapter(
            onItemClicked = { pos, itemMap, buttonType, url ->
                position = pos
                val appointment = itemMap["appointment"] as Appointment  // Casting to Appointment
                Bundle().apply {
                    putParcelable("appointment", appointment)
                }
                when(buttonType) {
                    0 -> viewModel.setAppointmentDeclined(appointment.id)
                    1 -> viewModel.setAppointmentAccepted(appointment.id)  // Assuming 'id' is part of Appointment
                    2 -> openImage(url)
                    3 -> openPdf(url)
                }
            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized){
            return binding.root
        }else {
            binding = FragmentAppointmentListingBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        ApointmentStatusObserver()

        val LinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewCitas.layoutManager = LinearLayoutManager
        binding.recyclerViewCitas.adapter = adapter
        viewModel.getAppointmentsBySpa()
    }

    private fun observer(){
        viewModel.appointmentsBySpa.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    //binding.progressBar.show()
                }
                is UiState.Failure -> {
                    //binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    //binding.progressBar.hide()
                    adapter.updateList(state.data)
                }
            }
        }
    }

    private fun ApointmentStatusObserver(){
        viewModel.setAppointmentStatus.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    //binding.progressBar.show()
                }
                is UiState.Failure -> {
                    //binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    //binding.progressBar.hide()
                    //observer()
                    toast(state.data)
                    //viewModel.getAppointmentsBySpa()
                    if (position != -1) {
                        adapter.removeItem(position)
                        position =-1
                    }
                }
            }
        }
    }


    private fun openImage(url: String) {
        lifecycleScope.launch {
            try {
                // Download the image and get a local URI
                val imageUri = downloadAndSaveImage(url)

                // If download is successful, show the image
                imageUri?.let { uri ->
                    ImageViewerUtil.showImage(
                        context = requireContext(),
                        uri = uri,
                        lifecycleScope = lifecycleScope,
                        onError = { errorMessage ->
                            toast(errorMessage)
                            Log.d("openImage", errorMessage)
                        }
                    )
                } ?: run {
                    toast("Failed to download image")
                }
            } catch (e: Exception) {
                toast("Error: ${e.localizedMessage}")
                Log.e("openImage", "Error downloading image", e)
            }
        }
    }

    private suspend fun downloadAndSaveImage(imageUrl: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // Use URL connection to download the image
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            // Read the input stream
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Save bitmap to external files directory
            val filename = "${System.currentTimeMillis()}_downloaded_image.jpg"
            val file = File(requireContext().getExternalFilesDir(null), filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // Convert file to Uri using Uri.fromFile
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("downloadImage", "Error downloading image", e)
            null
        }
    }


    private fun openPdf(url: String) {
        try {
            // Option 1: Using an external PDF viewer
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(url.toUri(), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if there's an app to handle PDF
            val packageManager = requireContext().packageManager
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Option 2: If no PDF viewer is found, prompt to download one
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("PDF Viewer Not Found")
                    .setMessage("Would you like to download a PDF viewer from the Play Store?")
                    .setPositiveButton("Yes") { _, _ ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.adobe.reader")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        } catch (e: Exception) {
            toast("Error opening PDF: ${e.localizedMessage}")
        }
    }
}