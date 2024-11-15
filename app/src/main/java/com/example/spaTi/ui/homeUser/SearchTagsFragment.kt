package com.example.spaTi.ui.homeUser

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spaTi.databinding.FragmentSearchTagsBinding
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.ui.tags.TagViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchTagsFragment : Fragment() {
    private val tagViewModel: TagViewModel by viewModels()
    private val serviceViewModel: ServiceViewModel by viewModels()
    private var _binding: FragmentSearchTagsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchTagsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
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

    private fun observeViewModels() {
        observeTagModels()
        observeServiceModels()
    }

    private fun cleanupObservers() {
        tagViewModel.tag.removeObservers(viewLifecycleOwner)
        serviceViewModel.service.removeObservers(viewLifecycleOwner)
    }

    private fun observeTagModels() {
        tagViewModel.tag.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
                is UiState.Success -> {
                    binding.progressBar.hide()
                    searchAdapter.updateTagsList(state.data)
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
            }
        }
    }

    private fun observeServiceModels() {
        serviceViewModel.service.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.show()
                is UiState.Success -> {
                    binding.progressBar.hide()
                    searchAdapter.updateServicesList(state.data)
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(
            onTagClicked = { tag ->
                serviceViewModel.getServicesByTagId(tag.id)
                binding.fragmentSearchTagsCancel.show()
            },
            onServiceClicked = { service ->

            }
        )
        binding.fragmentSearchTagsRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFragmentData() {
        tagViewModel.getTags()
        binding.fragmentSearchTagsCancel.hide()
    }

    private fun setupClickListeners() {
        binding.fragmentSearchTagsSearch.setOnEditorActionListener { _, actionId, event ->
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
        binding.fragmentSearchTagsSearch.addTextChangedListener(object : TextWatcher {
            private var searchJob: Job? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()

                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    s?.toString()?.let { searchText ->
                        if (searchText.isEmpty()) {
                            tagViewModel.getTags()
                            binding.fragmentSearchTagsCancel.hide()
                        } else {
                            tagViewModel.searchTags(searchText)
                            binding.fragmentSearchTagsCancel.show()
                        }
                    }
                }
            }
        })
        binding.fragmentSearchTagsCancel.setOnClickListener {
            tagViewModel.getTags()
            binding.fragmentSearchTagsCancel.hide()
        }
    }
}