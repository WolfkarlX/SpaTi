package com.example.spaTi.ui.Profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.spaTi.R
import com.example.spaTi.databinding.DialogImageBinding

class ImageDialogFragment : DialogFragment() {

    private lateinit var binding: DialogImageBinding
    private var imageUrl: String? = null

    companion object {
        const val ARG_IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUrl = arguments?.getString(ARG_IMAGE_URL)

        imageUrl?.let {
            // Limpiar el caché antes de cargar la imagen
            Glide.with(requireContext())
                .clear(binding.dialogImageView)

            Glide.with(requireContext())
                .load(it)
                .transform(CircleCrop())
                .placeholder(R.drawable.user_def)
                .error(R.drawable.user_def)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.dialogImageView)
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        return dialog
    }
}
