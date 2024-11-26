package com.example.spaTi.ui.spa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spaTi.R
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentServiceDetailBinding
import com.example.spaTi.databinding.FragmentSpaDetailBinding
import com.example.spaTi.ui.map.MapActivity2
import com.example.spaTi.util.UiState
import com.example.spaTi.util.convertMinutesToReadableTime
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.toastShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpaDetailFragment : Fragment() {

    private val viewModel: SpaViewModel by viewModels()
    private var _binding: FragmentSpaDetailBinding? = null
    private val binding get() = _binding!!
    private var isFavorite: Boolean = false
    private var objSpa: Spa? = null

    val servicesAdapter by lazy {
        SpaServicesAdapter { _, item ->
            Bundle().apply {
                item?.let { showServiceBottomSheet(it) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
        objSpa?.let { viewModel.getServicesBySpaId(it.id) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupObservers()
        _binding = null
    }

    private fun setupViews() {
        setupRecyclerView()
        setupFragmentData()
        setupClickListeners()
    }

    private fun setupFragmentData() {
        val spaFromRegular = arguments?.getParcelable<Spa>("spa")
        val spaFromFavorites = arguments?.getParcelable<Spa>("spaFavorites")
        val serviceSearchedFound = arguments?.getParcelable<Service>("service")

        when {
            spaFromRegular != null -> {
                initFragment(spaFromRegular, false, "")
            }
            spaFromFavorites != null -> {
                initFragment(spaFromFavorites, true, "")
            }
            serviceSearchedFound != null -> {
                getSpaByService(serviceSearchedFound) { spa, serviceName ->
                    spa?.let { initFragment(it, false, serviceName) }
                }
            }
            else -> {
                toast("Error: No spa data found")
                findNavController().navigateUp()
            }
        }
    }

    private fun initFragment(spa: Spa, isFavorite: Boolean, serviceName: String) {
        objSpa = spa
        //binding.spaDetailImage // here you set the image of the spa
        binding.spaDetailName.text = spa.spa_name
        binding.spaDetailLocation.text = spa.location
        if (!isFavorite) {
            binding.spaDetailFavBtn.setColorFilter(resources.getColor(R.color.white))
        }
        binding.spaDetailSearch.setText(serviceName)
        binding.spaDetailSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
    }

    private fun setupRecyclerView() {
        binding.spaDetailServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = servicesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.spaDetailBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.spaDetailFavBtn.setOnClickListener {
            // TODO: Set the favorites feature here
        }
        binding.spaDetailLocation.setOnClickListener {
            objSpa?.coordinates?.let { coordinates ->
                val latLon = coordinates.split(",")
                if (latLon.size == 2) {
                    val latitude = latLon[0].replace("Lat: ", "").trim().toDoubleOrNull()
                    val longitude = latLon[1].replace("Lon: ", "").trim().toDoubleOrNull()
                    if (latitude != null && longitude != null) {
                        val intent = Intent(requireContext(), MapActivity2::class.java).apply {
                            putExtra("latitude", latitude)
                            putExtra("longitude", longitude)
                        }
                        startActivity(intent)
                    } else {
                        toast("Coordenadas inválidas.")
                    }
                } else {
                    toast("Formato de coordenadas incorrecto.")
                }
            } ?: run {
                toast("No se encontraron coordenadas para este spa.")
            }
        }

        binding.spaDetailSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                true
            } else {
                false
            }
        }

        binding.spaDetailSearch.addTextChangedListener(object : TextWatcher {
            private var searchJob: Job? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    s?.toString()?.let { searchText ->
                        if (searchText.isEmpty()) {
                            objSpa?.let { viewModel.getServicesBySpaId(it.id) }
                        } else {
                            objSpa?.let { viewModel.searchServicesOnSpa(it.id, searchText) }
                        }
                    }
                }
            }
        })
    }

    private fun observeViewModels() {
        observeServiceOperations()
    }

    private fun observeServiceOperations() {
        viewModel.getServicesBySpaId.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
                is UiState.Success -> {
                    binding.progressBar.hide()
                    servicesAdapter.updateItems(state.data.toMutableList())
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
            }
        }
    }

    private fun getSpaByService(service: Service, onSpaReceived: (Spa?, String) -> Unit) {
        viewModel.getSpaById.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    onSpaReceived(state.data, service.name)
                }
                is UiState.Failure -> {
                    toast(state.error)
                }
                is UiState.Loading -> {
                    binding.progressBar.hide()
                }
            }
        }
        viewModel.getSpaById(service.spaId)
    }

    private fun cleanupObservers() {
        viewModel.getServicesBySpaId.removeObservers(viewLifecycleOwner)
    }

    private fun showServiceBottomSheet(service: Service) {
        BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            val bottomSheetBinding = FragmentServiceDetailBinding.inflate(layoutInflater)
            setContentView(bottomSheetBinding.root)

            val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            bottomSheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.45).toInt()

            behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED  // Start expanded
                isDraggable = true
                isHideable = true
            }

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            with(bottomSheetBinding) {
                val editable = Editable.Factory.getInstance()
                back.visibility = View.INVISIBLE
                titleTextView.text = "Detalles del Servicio"


                nameInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE)
                name.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                name.setTextColor(android.graphics.Color.BLACK)
                name.isEnabled = false
                name.text = editable.newEditable(service.name)

                priceInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE)
                price.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                price.setTextColor(android.graphics.Color.BLACK)
                price.isEnabled = false
                price.text = editable.newEditable(service.price.toString())

                durationInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE)
                durationInputLayout.hint = "Duración"
                duration.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                duration.setTextColor(android.graphics.Color.BLACK)
                duration.isEnabled = false
                duration.text = editable.newEditable(convertMinutesToReadableTime(service.durationMinutes))

                textViewTags.visibility = View.GONE
                tagsRecyclerView.visibility = View.GONE
                addTagBtn.visibility = View.GONE

                saveBtn.text = "Agendar cita"
                deleteBtn.visibility = View.GONE
                editBtn.visibility = View.GONE

                saveBtn.setOnClickListener {
                    dismiss()
                    findNavController().navigate(R.id.action_spaDetailFragment_to_appointmentFragment, Bundle().apply {
                        putParcelable("service", service)
                        putParcelable("spa", objSpa)
                    })
                }
            }

            show()
        }
    }
}
