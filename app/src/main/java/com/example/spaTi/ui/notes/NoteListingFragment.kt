package com.example.spaTi.ui.notes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentNoteListingBinding
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.ui.notes.NoteListingAdapter
import com.example.spaTi.ui.notes.NoteViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NoteListingFragment : Fragment() {

    val TAG: String = "NoteListingFragment"
    lateinit var binding: FragmentNoteListingBinding
    val viewModel: NoteViewModel by viewModels()
    val authViewModel: AuthViewModel by viewModels()
    val adapter by lazy {
        NoteListingAdapter(
            onItemClicked = { pos, item ->
                Bundle().apply {
                    putParcelable("note",item)
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "******************************************************")
        Log.e(TAG, "onCreate: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(TAG, "onCreateView: ")
        if (this::binding.isInitialized){
            return binding.root
        }else {
            binding = FragmentNoteListingBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "onViewCreated: ")
        oberver()
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        binding.recyclerView.adapter = adapter
//        binding.button.setOnClickListener {
//            findNavController().navigate(R.id.action_noteListingFragment_to_noteDetailFragment)
//        }
//        binding.logout.setOnClickListener {
//            authViewModel.logout {
//                findNavController().navigate(R.id.action_noteListingFragment_to_loginFragment)
//            }
//        }
//        binding.profileButton.setOnClickListener{
//            findNavController().navigate(R.id.action_noteListingFragment_to_myprofileFragment)
//
//        }
//
//        // MOVE THIS BINDING TO MOVE THE SERVICES CRUD
//        binding.testButton.setOnClickListener {
//            findNavController().navigate(R.id.action_noteListingFragment_to_servicesFragment)
//        }
        viewModel.getNotes()
    }

    private fun oberver(){
        viewModel.note.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    adapter.updateList(state.data.toMutableList())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "onDetach: ")
    }
}