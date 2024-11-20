package com.example.spaTi.ui.spahistoricalappointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.spaTi.R
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Report
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.databinding.FragmentSpaReportsDetailingBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.ui.auth.SpaAuthViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime


@AndroidEntryPoint
class SpaReportsDetailingFragment : Fragment() {

    var position = -1
    val TAG: String = "SpaReportsDetailingFragment"
    lateinit var binding: FragmentSpaReportsDetailingBinding
    val viewModel: ReportsViewModel by viewModels()
    private var itemPosition: Int? = null
    private var objAppointment: Appointment? = null
    private var objSpa: Spa? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized){
            return binding.root
        }else {
            binding = FragmentSpaReportsDetailingBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()

        objSpa = arguments?.getParcelable<Spa>("spa")
        objAppointment = arguments?.getParcelable<Appointment>("appointment")
        itemPosition = arguments?.getInt("position", -1)


        binding.btnSubmitReport.setOnClickListener {
            if(validation()) {
                viewModel.createReportfromSpa(getReportObj())
            }
        }
    }

    private fun validation(): Boolean {
        val regex = Regex("^[a-zA-Z0-9\\s.,!?'-]+$")

        if (binding.reportReason.text.toString().isNullOrEmpty()) {
            toast(getString(R.string.enter_reportreason))
            return false
        } else {
            if (binding.reportReason.text.toString().length < 10) {
                toast(getString(R.string.reportreason_short))
                return false
            }
            if (binding.reportReason.text.toString().length > 100) {
                toast(getString(R.string.reportreason_long))
                return false
            }
        }

        if (!regex.matches(binding.reportReason.text.toString())) {
            toast(getString(R.string.reportreason_hasspecialcharacters))
            return false
        }

        if(!isReasonNonAggressive(binding.reportReason.text.toString())){
            toast(getString(R.string.reportreason_isoffensive))
            return false
        }

        return true
    }


    private fun isReasonNonAggressive(reason: String): Boolean {
        val harmfulKeywords = setOf(
            // English words
            "hate", "violence", "abuse", "threat", "insult", "attack",
            "racist", "discrimination", "harm", "bully", "harass",
            "offend", "kill", "murder", "curse", "terrorist", "assault",

            // Spanish words (general and offensive slang)
            "odio", "violencia", "abuso", "amenaza", "insulto", "ataque",
            "racista", "discriminación", "daño", "acosar", "hostigar",
            "ofender", "matar", "asesinar", "maldecir", "terrorista", "agresión",
            "golpear", "lastimar", "perjudicar", "xenófobo", "intimidar",

            // Strong offensive slang (Spanish regional variations)
            "mamahuevo", "mamapinga", "pendejo", "pendeja", "cabron", "cabrona",
            "gilipollas", "estupido", "estupida", "idiota", "imbecil", "tarado",
            "puta", "puto", "zorra", "perra", "cabrón", "chingar", "chingada",
            "mierda", "coño", "carajo", "verga", "jodido", "joder", "malparido",
            "maldita", "maldito", "culero", "hijueputa", "desgraciado", "infeliz",
            "baboso", "babosa", "pelotudo", "forro", "lameculo", "cagado", "mamón"
        )

        val tokens = reason.lowercase().split("\\s+".toRegex())

        for (word in tokens) {
            if (harmfulKeywords.contains(word)) {
                return false // aggresive
            }
        }

        return true // Reason is non-aggressive
    }


    private fun observer(){
        viewModel.createReport.observe(viewLifecycleOwner) { state ->
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
                    toast(state.data)
                }
            }
        }
    }

    fun getReportObj(): Report {
        return Report(
            id = "",
            spaId = objSpa!!.id,
            userId = objAppointment!!.userId,
            reason = binding.reportReason.text.toString()
        )
    }


}