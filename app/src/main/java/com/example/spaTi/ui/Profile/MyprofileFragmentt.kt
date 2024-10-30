package com.example.spaTi.ui.Profile

import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentMyProfileBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyprofileFragmentt : Fragment() {

    val TAG: String = "MyprofileFragmentt"
    lateinit var binding: FragmentMyProfileBinding
    val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Call observer method to observe session state
        observer()
        viewModel.getSession()

        // Edit button navigation
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_myprofileFragment_to_editprofileFragment)
        }

        binding.saveButton.setOnClickListener {
            viewModel.logout{
                findNavController().navigate(R.id.action_myprofileFragment_to_loginFragment)
            }
        }
    }

    // Observe the session LiveData from the ViewModel
    fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress or loading indicator
                    binding.sessionProgress.show()
                }
                is UiState.Failure -> {
                    // Hide progress and show error message
                    binding.sessionProgress.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.sessionProgress.hide()
                    setData(state.data) // Call setData to update UI with user info
                }
            }
        }
    }

    // Update the UI with user session data
    fun setData(user: User?) {
        user?.let {
            binding.firstName.setText(it.first_name)
            binding.lastNames.setText(it.last_name)
            binding.email.setText(it.email)
            binding.phoneNumber.setText(it.cellphone)
            binding.bornday.setText(it.bornday)
            binding.sex.setText(it.sex)
        }
    }

    /*override fun onStart() {
        super.onStart()
        viewModel.getSession()
    }*/
}



/*fun getUserObj(): User {
        return User(
            id = "",
            first_name = binding.firstNameEt.text.toString(),
            last_name = binding.lastNameEt.text.toString(),
            email = binding.emailEt.text.toString(),
            type = "1",
        )
    }*/