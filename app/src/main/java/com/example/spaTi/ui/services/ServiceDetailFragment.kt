package com.example.spaTi.ui.services

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Service
import com.example.spaTi.databinding.FragmentServiceDetailBinding
import com.example.spaTi.databinding.FragmentServicesBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ServerTimestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

/**
 * ServiceDetailFragment displays the details of a selected service,
 * allowing users to edit or delete the service. It retrieves service
 * details from the arguments passed during navigation and updates the
 * UI accordingly. The user can add a new service or modify an existing one.
 *
 * Key functionalities:
 * - Observes state changes for adding, updating, and deleting services.
 * - Updates the UI based on the service object received.
 * - Provides buttons to save, delete, or edit the service.
 *
 * Lifecycle methods:
 * - [onCreateView]: Inflates the layout and initializes binding.
 * - [onViewCreated]: Sets up the user interface and starts observing data changes.
 * - [observer]: Watches for updates from the view model when adding, updating, or deleting a service, and displays relevant messages or actions.
 * - [updateUI]: Updates the UI based on whether it’s a new or existing service.
 * - [getService]: Gathers input from the user and creates a Service object.
 * - [disableUI] / [enableUI]: Controls whether the input fields are editable or read-only.
 */
@AndroidEntryPoint
class ServiceDetailFragment : Fragment() {
    val TAG: String = "ServiceDetailFragment"
    lateinit var binding: FragmentServiceDetailBinding
    val viewModel: ServiceViewModel by viewModels()
    var objService: Service? = null
    var tagsList: MutableList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            return binding.root
        } else {
            binding = FragmentServiceDetailBinding.inflate(layoutInflater)

            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
        observer()
    }

    private fun observer() {
        viewModel.addService.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    toast(state.data.second)
                    objService = state.data.first
                    disableUI()
                    binding.saveBtn.hide()
                    binding.deleteBtn.show()
                    binding.editBtn.show()
                }
            }
        }
        viewModel.updateService.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    toast(state.data)
                    binding.saveBtn.hide()
                    binding.deleteBtn.show()
                    disableUI()
                }
            }
        }
        viewModel.deleteService.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    toast(state.data)
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun updateUI() {
        var isForEdit = 0
        objService = arguments?.getParcelable("service")

        if (objService == null) {
            // New service
            binding.titleTextView.text = "Nuevo Servicio"
            binding.deleteBtn.visibility = View.GONE
            binding.editBtn.visibility = View.GONE
        } else {
            // Existing service
            binding.titleTextView.text = "Editar Servicio"
            binding.name.setText(objService?.name)
            binding.duration.setText(objService?.durationMinutes.toString())
            binding.price.setText(objService?.price.toString())
            binding.saveBtn.visibility = View.GONE
            disableUI()
        }
        binding.saveBtn.setOnClickListener {
            viewModel.addService(getService())
        }
        binding.deleteBtn.setOnClickListener {
            // Confirm and delete service
            objService?.let { viewModel.deleteService(it) }
        }
        binding.editBtn.setOnClickListener {
            if (isForEdit == 0) {
                isForEdit = 1
                enableUI()
            } else {
                isForEdit = 0
                viewModel.updateService(getService())
            }
        }
        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun getService(): Service {
        return Service(
            id = objService?.id ?: "",
            name = binding.name.text.toString().trim(),
            durationMinutes = binding.duration.text.toString().trim().toIntOrNull() ?: 0,
            price = binding.price.text.toString().trim().toDoubleOrNull() ?: 0.0,
        )
    }

    private fun disableUI() {
        binding.nameInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE)
        binding.name.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.name.setTextColor(android.graphics.Color.BLACK)
        binding.name.isEnabled = false

        binding.priceInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE)
        binding.price.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.price.setTextColor(android.graphics.Color.BLACK)
        binding.price.isEnabled = false

        binding.durationInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE)
        binding.duration.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.duration.setTextColor(android.graphics.Color.BLACK)
        binding.duration.isEnabled = false

        binding.addCategoryBtn.visibility = View.GONE
        binding.categories.isEnabled = false
    }

    private fun enableUI() {
        binding.nameInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
        binding.name.setBackgroundResource(android.R.color.transparent)
        binding.name.setTextColor(resources.getColor(android.R.color.black, null))
        binding.name.isEnabled = true

        binding.priceInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
        binding.price.setBackgroundResource(android.R.color.transparent)
        binding.price.setTextColor(resources.getColor(android.R.color.black, null))
        binding.price.isEnabled = true

        binding.durationInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
        binding.duration.setBackgroundResource(android.R.color.transparent)
        binding.duration.setTextColor(resources.getColor(android.R.color.black, null))
        binding.duration.isEnabled = true

        binding.addCategoryBtn.visibility = View.VISIBLE

        binding.categories.isEnabled = true
    }
}