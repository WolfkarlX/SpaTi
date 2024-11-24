package com.example.spaTi.ui.SpaProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.spaTi.R
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.FragmentMyAccountSpaBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyAccountSpaFragment : Fragment() {
    val TAG: String = "MyAccountSpaFragment"
    val viewModel: MySpaViewModel by viewModels()

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

        observer()

        viewModel.syncSessionWithDatabase()

        binding.home.setOnClickListener {
            findNavController().navigate(R.id.action_MyAccountSpaFragment_to_spaHomeFragment)
        }

        binding.profileTools.setOnClickListener {
            findNavController().navigate(R.id.action_MyAccountSpaFragment_to_myspaFragment)
        }
    }

    fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    binding.sessionProgress.hide()
                    toast(state.error)
                    viewModel.logout {
                        findNavController().navigate(R.id.action_MyAccountSpaFragment_to_loginFragment)
                    }
                }
                is UiState.Success -> {
                    binding.sessionProgress.hide()
                    setData(state.data)
                }
            }
        }
    }

    fun setData(spa: Spa?) {
        spa?.let {
            binding.spaNameEt.setText(it.spa_name)
            binding.spaEmailEt.setText(it.email)

            Glide.with(requireContext())
                .load(it.profileImageUrl)
                .into(binding.imageview)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
