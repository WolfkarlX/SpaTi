package com.example.spaTi.ui.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.spaTi.R
import com.example.spaTi.databinding.FragmentServicesBinding
import com.example.spaTi.databinding.AdminSpaBinding
import com.example.spaTi.ui.auth.AuthViewModel
import com.example.spaTi.ui.services.ServicesAdapter
import com.example.spaTi.ui.services.ServiceViewModel
import com.example.spaTi.util.UiState
import com.example.spaTi.util.hide
import com.example.spaTi.util.show
import com.example.spaTi.util.toast
import dagger.hilt.android.AndroidEntryPoint

/**
 * ServicesFragment displays a list of services and allows navigation to
 * the detail view for each service. It handles the retrieval of services
 * through the ServiceViewModel and observes the loading state to update
 * the UI accordingly. The user can click on a service to view or edit
 * its details.
 *
 * The fragment has the following key features:
 * - Uses a staggered grid layout for displaying services.
 * - Navigates to ServiceDetailFragment when a service is clicked.
 * - Observes service loading states and updates the UI based on the state.
 *
 * Lifecycle methods:
 * - [onAttach]: Called when the fragment is attached to its context.
 * - [onCreate]: Initializes the fragment.
 * - [onCreateView]: Inflates the fragment's view and initializes binding.
 * - [onViewCreated]: Sets up the observer and initializes the RecyclerView.
 * - [onStart], [onResume], [onPause], [onStop], [onDestroyView], [onDestroy], [onDetach]:
 * - [observer]: Watches the view model for updates to show progress, errors, or the service list.
 */
@AndroidEntryPoint
class ServicesFragment : Fragment() {
    val TAG: String = "ServicesFragment"

    lateinit var binding: AdminSpaBinding
    val viewModel: ServiceViewModel by viewModels()
    val authViewModel: AuthViewModel by viewModels()
    val adapter by lazy {
        ServicesAdapter{ pos, item ->
            if ( item == null ){
                findNavController().navigate(R.id.action_servicesFragment_to_serviceDetailFragment)
            } else {
                findNavController().navigate(R.id.action_servicesFragment_to_serviceDetailFragment, Bundle().apply {
                    putParcelable("service", item)
                })
            }
        }
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(TAG, "onCreateView: ")
        if (this::binding.isInitialized) {
            return binding.root
        } else {

            binding = AdminSpaBinding.inflate(layoutInflater)
            return binding.root
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