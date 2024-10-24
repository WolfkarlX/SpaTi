package com.example.spaTi.ui.homeUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.spaTi.databinding.FragmentTagsListBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TagsListFragment : Fragment() {
    val TAG: String = "UserHomeFragment"

    private var _binding: FragmentTagsListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}