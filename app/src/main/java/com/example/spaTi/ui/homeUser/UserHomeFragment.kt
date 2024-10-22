package com.example.spaTi.ui.homeUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentUserHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserHomeFragment : Fragment() {
    val TAG: String = "UserHomeFragment"

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.favorites -> {
                    findNavController().navigate(R.id.action_userHomeFragment_to_favoritesFragment)
                    true
                }
                R.id.tags -> {
                    findNavController().navigate(R.id.action_userHomeFragment_to_tagsListFragment)
                    true
                }
                R.id.home -> { true }
                R.id.appointments -> {
                    findNavController().navigate(R.id.action_userHomeFragment_to_appointmentsFragment)
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.action_userHomeFragment_to_myProfileFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}