package com.example.spaTi.ui.Profile

import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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
import com.example.spaTi.util.isValidEmail
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import com.example.spaTi.util.validatePassword
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        
        val opcionesSexo = arrayOf("Hombre", "Mujer")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, opcionesSexo)
        binding.sexoEt.setAdapter(adapter)

        binding.sexoEt.setOnClickListener {
            binding.sexoEt.showDropDown()
        }

        observer()
        viewModel.getSession()

        binding.btnGuardar.setOnClickListener {
            if(validation()){
                observeEdit()

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
            reports = binding.reportsEt.text.toString(),
            type = "1",
        )
    }

    fun validation(): Boolean {
        if (binding.etNombre.text.isNullOrEmpty()){
            toast(getString(R.string.enter_first_name))
            return false
        }

        if (binding.lastNameEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_last_name))
            return false
        }

        if (binding.telefonoEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_cellphone))
            return false
        } else {
            if (binding.telefonoEt.text.toString().length < 10 || binding.telefonoEt.text.toString().length > 15) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }

            val phoneNumber = binding.telefonoEt.text.toString()


            if (phoneNumber.toSet().size < 4) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }

            if (phoneNumber.startsWith("0") || phoneNumber.startsWith("1")) {
                toast(getString(R.string.invalid_cellphone_number))
                return false
            }
        }

        if (binding.sexoEt.text.isNullOrEmpty()){
            toast(getString(R.string.enter_sex))
            return false
        }

        if (binding.etDia.text.isNullOrEmpty() || binding.etMes.text.isNullOrEmpty() || binding.etAno.text.isNullOrEmpty()) {
            when {
                binding.etDia.text.isNullOrEmpty() -> toast(getString(R.string.enter_day))
                binding.etMes.text.isNullOrEmpty() -> toast(getString(R.string.enter_month))
                binding.etAno.text.isNullOrEmpty() -> toast(getString(R.string.enter_year))
            }
            return false
        }else{

            val day = binding.etDia.text.toString().toIntOrNull()
            if (day == null || day <= 0 || day > 31) {
                toast(getString(R.string.invalid_day))
                return false
            }

            // Validate month (1-12)
            val month = binding.etMes.text.toString().toIntOrNull()
            if (month == null || month <= 0 || month > 12) {
                toast(getString(R.string.invalid_month))
                return false
            }

            val year = binding.etAno.text.toString().toIntOrNull()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year == null || year <= 1900 || year >= currentYear) {
                toast(getString(R.string.invalid_year))
                return false
            }

            age = getAge(binding.etDia.text.toString().toInt(), binding.etMes.text.toString().toInt(), binding.etAno.text.toString().toInt())
            if (age < 18) {
                toast(getString(R.string.invalid_age))
                return false
            }
        }

        return true
    }

    fun observer() {
        viewModel.session.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    setData(state.data)
                }
            }
        }
    }

    fun observeEdit() {
        viewModel.editUser.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    toast(state.data)
                    CleanInputs()
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


            binding.reportsEt.setText(it.reports)

            val opcionesSexo = arrayOf("Hombre", "Mujer")
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, opcionesSexo)
            binding.sexoEt.setAdapter(adapter)
        }
    }



    fun CleanInputs() {
        binding.etNombre.setText("")
        binding.lastNameEt.setText("")
        binding.telefonoEt.setText("")
        binding.sexoEt.setText("")
        binding.etDia.setText("")
        binding.etMes.setText("")
        binding.etAno.setText("")
    }
}