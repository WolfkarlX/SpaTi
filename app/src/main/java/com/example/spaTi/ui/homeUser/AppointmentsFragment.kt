package com.example.spaTi.ui.homeUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.spaTi.databinding.FragmentAppointmentsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppointmentsFragment : Fragment() {
    val TAG: String = "UserHomeFragment"

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}