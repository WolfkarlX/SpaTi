package com.example.spaTi.ui.Profile

import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spaTi.R
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentEditProfileBinding
import com.example.spaTi.util.UiState
import com.example.spaTi.util.extractNumbersFromDate
import com.example.spaTi.util.getAge
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    var id = ""
    var email = ""
    var age = 0
    val TAG: String = "EditprofileFragment"
    lateinit var binding: FragmentEditProfileBinding
    val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val opcionesSexo = arrayOf("Hombre", "Mujer")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, opcionesSexo)
        binding.sexoEt.setAdapter(adapter)

        binding.sexoEt.setOnClickListener {
            binding.sexoEt.showDropDown()
        }

        // Call observer method to observe session state
        observer()
        viewModel.getSession()

        // Edit button navigation
        binding.btnGuardar.setOnClickListener {
            if(validation()){
                observeEdit()

                val user = getUserObj()
                viewModel.editUser(user = getUserObj())
            }
        }

        binding.btnCancelar.setOnClickListener {
            findNavController().navigate(R.id.action_editprofileFragment_to_myprofileFragment)
        }

    }

    fun getUserObj(): User {
        return User(
            id = id,
            email = email,
            first_name = binding.etNombre.text.toString(),
            last_name = binding.lastNameEt.text.toString(),
            cellphone = binding.telefonoEt.text.toString(),
            sex = binding.sexoEt.text.toString(),
            bornday = "" + binding.etDia.text.toString() + "/"+ binding.etMes.text.toString()+ "/" + binding.etAno.text.toString(),
            age = age.toString(),
            type = "1",
        )
    }

    fun validation():Boolean{
        var isValid = true

        age = getAge(binding.etDia.text.toString().toInt(), binding.etMes.text.toString().toInt(), binding.etAno.text.toString().toInt())
        if (age < 18) {
            isValid = false
            toast(getString(R.string.invalid_age))
        }

        return isValid
    }

    // Observe the session LiveData from the ViewModel
    fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress or loading indicator
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    // Hide progress and show error message
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.progressBar.hide()
                    setData(state.data) // Call setData to update UI with user info
                }
            }
        }
    }

    fun observeEdit() {
        viewModel.editUser.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress or loading indicator
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    // Hide progress and show error message
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    // Hide progress and display user data
                    binding.progressBar.hide()
                    toast(state.data)
                    findNavController().navigate(R.id.action_editprofileFragment_to_myprofileFragment)

                }
            }
        }
    }

    fun setData(user: User?) {
        user?.let {
            val bornday = extractNumbersFromDate(it.bornday)
            email = it.email

            id = it.id
            binding.etNombre.setText(it.first_name)
            binding.lastNameEt.setText(it.last_name)
            binding.telefonoEt.setText(it.cellphone)
            binding.sexoEt.setText(it.sex)
            binding.etDia.setText(bornday[0])
            binding.etMes.setText(bornday[1])
            binding.etAno.setText(bornday[2])

        }
    }
}