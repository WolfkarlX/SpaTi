package com.example.spaTi.ui.tags

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spaTi.data.models.Tag
import com.example.spaTi.ui.services.TagSelectionListener
import com.example.spaTi.databinding.FragmentTagsBinding
import com.example.spaTi.ui.services.ServiceDetailFragment
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.toastShort
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TagsFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TagViewModel by activityViewModels()
    private var tagSelectionListener: TagSelectionListener? = null
    private lateinit var selectedAdapter: TagsSelectedAdapter
    private lateinit var featuredAdapter: TagsFeaturedAdapter

    private var selectedTags: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            selectedTags = args.getStringArrayList(ARG_SELECTED_TAGS)
        }

        initializeAdapter()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { bottomSheet ->
                val behaviour = BottomSheetBehavior.from(bottomSheet)
                bottomSheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.85).toInt()
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeAction()

        selectedAdapter.clearItems()
        selectedTags?.takeIf { it.isNotEmpty() }?.forEach { itemId ->
            viewModel.getTagById(itemId)
        } ?: run {
            binding.textViewTagsSelected.visibility = View.GONE
        }

        viewModel.getTags()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedAdapter.clearItems()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.d("TAGSFRAGMENT", selectedAdapter.getItems().toString())
        super.onDismiss(dialog)
        selectedAdapter.clearItems()
        Log.d("TAGSFRAGMENT", selectedAdapter.getItems().toString())
    }

    private fun initializeAdapter() {
        featuredAdapter = TagsFeaturedAdapter { _, item ->
            item?.let { handleTagFeaturedClick(it) }
        }

        selectedAdapter = TagsSelectedAdapter(
            onItemClicked = { pos, _ ->
                handleTagSelectedClick(pos)
            }
        )
    }

    private fun observeAction() {
        viewModel.getTagById.removeObservers(viewLifecycleOwner)
        viewModel.tag.removeObservers(viewLifecycleOwner)
        viewModel.addTag.removeObservers(viewLifecycleOwner)

        viewModel.tag.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    binding.textViewTagsFeatured.hide()

                    toast(state.error)
                }
                is UiState.Success -> {
                    if (state.data.isNotEmpty()){
                        binding.textViewTagsFeatured.show()
                    }

                    featuredAdapter.updateItems(state.data.toMutableList())
                }
            }
        }
        viewModel.getTagById.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    binding.textViewTagsSelected.hide()

                    toast(state.error)
                }
                is UiState.Success -> {
                    state.data?.let { tag ->
                        binding.textViewTagsSelected.show()
                        selectedAdapter.addItem(tag)
                    }
                }
            }
        }
        viewModel.addTag.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    toast(state.error)
                }
                is UiState.Success -> {
                    val (updatedTag, message) = state.data
                    selectedAdapter.updateTagWithId(updatedTag)
                }
            }
        }
    }

    private fun setupAdapters() {
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        binding.selectedRecyclerView.adapter = selectedAdapter
        binding.selectedRecyclerView.layoutManager = flexboxLayoutManager

        binding.featuredRecyclerView.adapter = featuredAdapter
        binding.featuredRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val textWrote = binding.searchEditText.text.toString().trim()
                val newTag = createTag(textWrote)

                if (textWrote.isNotEmpty() && !selectedAdapter.isExists(newTag)) {
                    binding.textViewTagsSelected.show()
                    selectedAdapter.addItem(newTag)

                    hideKeyboard()
                } else {
                    toastShort("Tag already selected or is empty")
                }
                binding.searchEditText.text?.clear()
                binding.searchEditText.requestFocus()
                true
            } else {
                false
            }
        }
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            private var searchJob: Job? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Cancel previous job if it exists
                searchJob?.cancel()

                // Start new search after a delay
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300) // Add a 300ms delay to avoid too frequent searches
                    s?.toString()?.let { searchText ->
                        // Your search logic here
                        if (searchText.isEmpty()) {
                            viewModel.getTags()
                        } else {
                            viewModel.searchTags(searchText)
                        }
                    }
                }
            }
        })
        binding.cancelBtn.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
            (parentFragment as? ServiceDetailFragment)?.clearBottomSheetReference()
            dismiss()
        }
        binding.submitBtn.setOnClickListener {
            val tagsSubmitted = selectedAdapter.getItems()

            if (tagsSubmitted.isEmpty()) {
                tagSelectionListener?.onTagsSelected(emptyList())
                (parentFragment as? ServiceDetailFragment)?.clearBottomSheetReference()
                dismiss()
                return@setOnClickListener
            }

            var updatedCount = 0
            val totalTags = tagsSubmitted.size

            tagsSubmitted.forEach { tag ->
                if (tag.id.isEmpty()) {
                    viewModel.addTag(tag)
                } else {
                    viewModel.updateTag(tag)
                }
                updatedCount++

                if (updatedCount == totalTags) {
                    tagSelectionListener?.onTagsSelected(selectedAdapter.getItems())
                    (parentFragment as? ServiceDetailFragment)?.clearBottomSheetReference()
                    dismiss()
                }
            }
        }
    }

    private fun handleTagFeaturedClick(tag: Tag) {
        if (!selectedAdapter.isExists(tag)) {
            binding.textViewTagsSelected.show()
            selectedAdapter.addItem(tag)
        } else {
            toastShort("Tag already selected")
        }
    }

    private fun handleTagSelectedClick(position: Int) {
        selectedAdapter.removeItem(position)
        if (selectedAdapter.itemCount == 0) {
            binding.textViewTagsSelected.hide()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun createTag(name : String): Tag {
        return Tag(
            id = "",
            name = name,
            relatedCount = 1
        )
    }

    fun setTagSelectionListener(listener: TagSelectionListener) {
        tagSelectionListener = listener
    }

    companion object {
        const val TAG = "TagBottomSheetFragment"
        private const val ARG_SELECTED_TAGS = "arg_selected_tags"

        fun newInstance(
            selectedTags: List<String>? = null
        ): TagsFragment {
            return TagsFragment().apply {
                arguments = Bundle().apply {
                    selectedTags?.let { tags ->
                        putStringArrayList(ARG_SELECTED_TAGS, ArrayList(tags))
                    }
                }
            }
        }
    }
}