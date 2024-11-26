package com.example.spaTi.ui.appointments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.spaTi.ui.appointments.FileUploadHelper.FileType
import com.yalantis.ucrop.UCrop
import java.io.File

class FileUploadHelper(
    private val fragment: Fragment,
    private val onImagePickingStarted: () -> Unit = {},
    private val onImagePickingCancelled: (Int) -> Unit = { _ -> },
    private val handleFileResult: (Uri, FileType) -> Unit, // Modified to include file type
) {
    private var photoUri: Uri? = null

    // Configurable properties
    var aspectRatioX: Float = 1f
    var aspectRatioY: Float = 1f
    var maxResultWidth: Int = 800
    var maxResultHeight: Int = 800
    var compressionQuality: Int = 90
    var shouldCrop: Boolean = true  // New option to control cropping
    var fileType: FileType = FileType.IMAGE_ONLY  // New option to control file type selection

    // Enum for file type selection
    enum class FileType {
        IMAGE_ONLY,
        PDF_ONLY,
        IMAGE_OR_PDF,
        ALL_FILES
    }

    // Companion for result codes dictionary
    companion object {
        const val PROCESSING_IN_PROGRESS = 100
        const val SUCCESS = 200
        const val CANCELLED = 204
        const val PERMISSION_DENIED = 300
        const val PERMISSION_PERMANENTLY_DENIED = 301
        const val BAD_REQUEST = 400
        const val NOT_FOUND = 404
        const val CONFLICT = 409
        const val UNSUPPORTED_FORMAT = 415
        const val UNKNOWN_ERROR = 500
        const val NOT_IMPLEMENTED = 501
    }

    // Configurable dialog options
    var dialogTitle: String = "Select Option"
    var cameraOption: String = "Take Photo"
    var galleryOption: String = "Choose from Gallery"

    // Permission request code
    protected val IMAGE_PICK_CODE = 1000
    protected val REQUEST_CAMERA_PERMISSION = 1001

    // Launchers
    private val cameraLauncher: ActivityResultLauncher<Intent> = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                processFile(uri)
            }
        } else {
            processError(result.resultCode)
        }
    }

    private val galleryLauncher: ActivityResultLauncher<Intent> = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val sourceUri = result.data?.data
            sourceUri?.let { uri ->
                try {
                    processFile(uri)
                } catch (e: Exception) {
                    // Handle specific file-related errors
                    onImagePickingCancelled(handleFileError(e))
                }
            }
        } else {
            processError(result.resultCode)
        }
    }

    private val cropLauncher: ActivityResultLauncher<Intent> = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let { processFile(it) }
            }
            else -> processError(result.resultCode)
        }
    }

    private fun processFile(sourceUri: Uri) {
        val contentResolver = fragment.requireContext().contentResolver
        val mimeType = contentResolver.getType(sourceUri)

        // Validate file type
        val actualFileType = when {
            mimeType?.startsWith("image/") == true -> FileType.IMAGE_ONLY
            mimeType == "application/pdf" -> FileType.PDF_ONLY
            else -> FileType.ALL_FILES
        }

        // Check if the file type matches the expected type
        if (!isFileTypeAllowed(actualFileType)) {
            throw SecurityException("Unsupported file type")
        }

        // For images, proceed with existing crop logic
        if (actualFileType == FileType.IMAGE_ONLY && shouldCrop) {
            startCrop(sourceUri)
        } else {
            // For PDFs or other files, directly handle the file
            handleFileResult(sourceUri, actualFileType)
        }
    }

    private fun isFileTypeAllowed(actualFileType: FileType): Boolean {
        return when (fileType) {
            FileType.IMAGE_ONLY -> actualFileType == FileType.IMAGE_ONLY
            FileType.PDF_ONLY -> actualFileType == FileType.PDF_ONLY
            FileType.IMAGE_OR_PDF ->
                actualFileType == FileType.IMAGE_ONLY || actualFileType == FileType.PDF_ONLY
            FileType.ALL_FILES -> true
        }
    }

    private fun handleFileError(e: Exception): Int {
        return when (e) {
            is SecurityException -> UNSUPPORTED_FORMAT
            is IllegalArgumentException -> BAD_REQUEST
            is NullPointerException -> NOT_FOUND
            is UnsupportedOperationException -> NOT_IMPLEMENTED
            else -> UNKNOWN_ERROR
        }
    }

    private fun processError(status: Int) {
        val errorCode = when (status) {
            Activity.RESULT_CANCELED -> CANCELLED
            PackageManager.PERMISSION_DENIED -> PERMISSION_DENIED
            else -> UNKNOWN_ERROR
        }
        onImagePickingCancelled(errorCode)
    }

    fun showImageSourceDialog() {
        onImagePickingStarted()
        val options = arrayOf(cameraOption, galleryOption)
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(dialogTitle)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        val context = fragment.requireContext()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            // Check if we should show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    fragment.requireActivity(),
                    Manifest.permission.CAMERA
                )) {
                // User has previously denied permission, but not permanently
                ActivityCompat.requestPermissions(
                    fragment.requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                // First time request or permanently denied
                ActivityCompat.requestPermissions(
                    fragment.requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // Check if the user has permanently denied the permission
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    fragment.requireActivity(),
                    Manifest.permission.CAMERA
                )

                if (!shouldShowRationale) {
                    // Permission is permanently denied
                    // Provide a way for the user to go to app settings
                    onImagePickingCancelled(PERMISSION_PERMANENTLY_DENIED)
                } else {
                    // User simply denied the permission this time
                    processError(PackageManager.PERMISSION_DENIED)
                }
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = getPhotoFile(fragment.requireContext())
            photoUri = FileProvider.getUriForFile(
                fragment.requireContext(),
                "${fragment.requireContext().packageName}.provider",
                photoFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                // Add this line to grant write permission
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            // Check if there's an app that can handle the camera intent
            if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
                cameraLauncher.launch(intent)
            } else {
                // No camera app available
                onImagePickingCancelled(NOT_IMPLEMENTED)
            }
        } catch (e: Exception) {
            // Handle potential file creation or URI generation errors
            onImagePickingCancelled(handleFileError(e))
        }
    }

    private fun pickImageFromGallery() {
        val intent = when (fileType) {
            FileType.IMAGE_ONLY -> {
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            }
            FileType.PDF_ONLY -> {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/pdf"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            }
            FileType.IMAGE_OR_PDF -> {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
                }
            }
            FileType.ALL_FILES -> {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            }
        }
        galleryLauncher.launch(intent)
    }

    // Modify the crop method to handle non-image files
    private fun startCrop(sourceUri: Uri) {
        // Only crop if it's an image
        val contentResolver = fragment.requireContext().contentResolver
        val mimeType = contentResolver.getType(sourceUri)

        if (mimeType?.startsWith("image/") != true) {
            handleFileResult(sourceUri, FileType.PDF_ONLY)
            return
        }

        val destinationUri = Uri.fromFile(getCroppedFile(fragment.requireContext()))

        val cropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(aspectRatioX, aspectRatioY)
            .withMaxResultSize(maxResultWidth, maxResultHeight)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(compressionQuality)
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
            })
            .getIntent(fragment.requireContext())

        cropLauncher.launch(cropIntent)
    }

    private fun getPhotoFile(context: Context): File =
        File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")

    private fun getCroppedFile(context: Context): File =
        File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
}

// Extension function to simplify usage
fun Fragment.createFileUploadHelper(
    onImagePickingStarted: () -> Unit = {},
    onImagePickingCancelled: (Int) -> Unit = { _ -> },
    handleFileResult: (Uri, FileType) -> Unit,
): FileUploadHelper = FileUploadHelper(
    fragment = this,
    onImagePickingStarted = onImagePickingStarted,
    onImagePickingCancelled = onImagePickingCancelled,
    handleFileResult = handleFileResult,
)