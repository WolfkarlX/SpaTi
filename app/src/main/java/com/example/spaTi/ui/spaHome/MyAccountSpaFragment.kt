package com.example.spaTi.ui.spaHome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentMyAccountSpaBinding
import com.example.spaTi.databinding.FragmentSpaHomeBinding
import com.example.spaTi.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyAccountSpaFragment : Fragment() {
    val TAG: String = "MyAccountSpaFragment"
    val authViewModel: AuthViewModel by viewModels()

    private var _binding: FragmentMyAccountSpaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountSpaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.home.setOnClickListener {
            findNavController().navigate(R.id.action_MyAccountSpaFragment_to_spaHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}