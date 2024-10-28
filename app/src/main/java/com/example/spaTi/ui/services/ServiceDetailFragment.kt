package com.example.spaTi.ui.services

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Tag
import com.example.spaTi.data.repository.SpaAuthRepository
import com.example.spaTi.databinding.FragmentServiceDetailBinding
import com.example.spaTi.ui.tags.TagViewModel
import com.example.spaTi.ui.tags.TagsFragment
import com.example.spaTi.ui.tags.TagsSelectedAdapter
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

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
 * - [observeAction]: Watches for updates from the view model when adding, updating, or deleting a service, and displays relevant messages or actions.
 * - [updateUI]: Updates the UI based on whether it’s a new or existing service.
 * - [getService]: Gathers input from the user and creates a Service object.
 * - [disableUI] / [enableUI]: Controls whether the input fields are editable or read-only.
 */
@AndroidEntryPoint
class ServiceDetailFragment : Fragment(), TagSelectionListener {
    private val viewModel: ServiceViewModel by viewModels()
    private val tagViewModel: TagViewModel by activityViewModels()
    private var objService: Service = Service(
        id = "",
        name = "",
        durationMinutes = 0,
        price = 0.0,
        tags = emptyList(),
        spaId = ""
    )
    private var serviceState: ServiceState = ServiceState.Idle
    private var bottomSheetFragment: TagsFragment? = null
    private lateinit var adapter: TagsSelectedAdapter

    private var _binding: FragmentServiceDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TagsSelectedAdapter(
            onItemClicked = { pos, item ->
                Bundle().apply {
                    putParcelable("tag", item)
                }
            },
            hideCloseButton = true,
            customTextColor = resources.getColor(R.color.verde),
            customBgColor = resources.getColor(R.color.white),
            customStrokeColor = resources.getColor(R.color.verde)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.addService.removeObservers(viewLifecycleOwner)
        viewModel.updateService.removeObservers(viewLifecycleOwner)
        viewModel.deleteService.removeObservers(viewLifecycleOwner)
        tagViewModel.getTagById.removeObservers(viewLifecycleOwner)

        adapter.clearItems()

        clearBottomSheetReference()

        _binding = null
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet()
        setupAdapter()
        updateUI()
        observeAction()
    }

    override fun onTagsSelected(tags: List<Tag>) {
        adapter.clearItems()
        adapter.updateItems(tags.toMutableList())

        objService = objService.copy(tags = tags.map { it.id })

        bottomSheetFragment = null
        binding.textViewTags.visibility = if (tags.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeAction() {
        viewModel.addService.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.saveBtn.hide()
                    binding.deleteBtn.show()
                    binding.editBtn.show()
                    objService = state.data.first
                    disableUI()

                    serviceState = ServiceState.Idle
                    toast(state.data.second)
                }
            }
        }
        viewModel.updateService.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.saveBtn.hide()
                    binding.deleteBtn.show()
                    disableUI()

                    serviceState = ServiceState.Idle
                    toast(state.data)
                }
            }
        }
        viewModel.deleteService.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
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
        tagViewModel.getTagById.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    state.data?.let { tag ->
                        binding.textViewTags.show()
                        adapter.addItem(tag)
                    }
                }
            }
        }
    }

    private fun setupBottomSheet() {
        binding.addTagBtn.setOnClickListener {
            bottomSheetFragment = TagsFragment.newInstance(objService.tags).apply {
                setTagSelectionListener(this@ServiceDetailFragment)
            }
            bottomSheetFragment?.show(childFragmentManager, TagsFragment.TAG)
        }
    }

    private fun setupAdapter() {
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        binding.tagsRecyclerView.adapter = adapter
        binding.tagsRecyclerView.layoutManager = flexboxLayoutManager
    }

    private fun updateUI() {
        val serviceFromArgs = arguments?.getParcelable<Service>("service")

        binding.textViewTags.visibility = View.GONE
        adapter.clearItems()

        if (serviceFromArgs != null) {
            objService = serviceFromArgs
        } else {
            objService = Service(id = "", name = "", durationMinutes = 0, price = 0.0, tags = emptyList())
        }

        isNewService()

        binding.saveBtn.setOnClickListener {
            if (checkInputs()) {
                viewModel.addService(getService())
            } else {
                toast("Fill all inputs with valid data")
            }
        }
        binding.deleteBtn.setOnClickListener {
            objService.let { viewModel.deleteService(it) }
        }
        binding.editBtn.setOnClickListener {
            when (serviceState) {
                is ServiceState.Idle -> {
                    serviceState = ServiceState.Editing
                    enableUI()
                }
                is ServiceState.Editing -> {
                    serviceState = ServiceState.Idle
                    viewModel.updateService(getService())
                }
            }
        }
        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        Log.d("TAGSSELECTEDADAPTER serviceDetailFragment objService", objService.toString())
        if (objService.tags.isNotEmpty()) {
            binding.textViewTags.visibility = View.VISIBLE
            objService.tags.forEach { tagId ->
                tagViewModel.getTagById(tagId)
            }
        }
    }

    private fun isNewService() {
        if (objService.id.isEmpty()) {
            binding.titleTextView.text = "Nuevo Servicio"
            binding.addTagBtn.text = "Agregar Etiquetas"
            binding.deleteBtn.visibility = View.GONE
            binding.editBtn.visibility = View.GONE
        } else {
            binding.titleTextView.text = "Editar Servicio"
            binding.addTagBtn.text = "Editar Etiquetas"
            binding.name.setText(objService.name)
            binding.duration.setText(objService.durationMinutes.toString())
            binding.price.setText(objService.price.toString())

            binding.saveBtn.visibility = View.GONE

            disableUI()
        }
    }

    private fun getService(): Service {
        return objService.copy(
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

        binding.addTagBtn.visibility = View.GONE
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

        binding.addTagBtn.visibility = View.VISIBLE
    }

    private fun checkInputs(): Boolean {
        val name = binding.name.text.toString().trim()
        val duration = binding.duration.text.toString().trim().toIntOrNull() ?: 0
        val price = binding.price.text.toString().trim().toDoubleOrNull() ?: 0.0

        return (name.isNotEmpty() && duration != 0 && price != 0.0)
    }

    fun clearBottomSheetReference() {
        bottomSheetFragment = null
    }
}

sealed class  ServiceState {
    object Idle : ServiceState()
    object Editing : ServiceState()
}

interface TagSelectionListener {
    fun onTagsSelected(tags: List<Tag>)
}