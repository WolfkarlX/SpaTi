package com.example.spaTi.ui.homeUser

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.spaTi.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class ImageViewerUtil {
    companion object {
        /**
         * Opens a full-screen image viewer dialog
         */
        fun showImage(
            context: Context,
            uri: Uri,
            lifecycleScope: LifecycleCoroutineScope,
            onError: ((String) -> Unit)? = null,
            @StyleRes dialogTheme: Int = android.R.style.Theme_Black_NoTitleBar_Fullscreen
        ) {
            try {
                val dialog = Dialog(context, dialogTheme)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.dialog_image_viewer)

                dialog.window?.apply {
                    setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setBackgroundDrawable(ColorDrawable(Color.BLACK))
                }

                val imageView = dialog.findViewById<ZoomableImageView>(R.id.fullscreen_image)
                val closeButton = dialog.findViewById<MaterialButton>(R.id.btn_close_image)
                val progressBar = dialog.findViewById<ProgressBar>(R.id.image_progress_bar)

                closeButton.setOnClickListener { dialog.dismiss() }

                loadImageManually(
                    context,
                    uri,
                    imageView,
                    progressBar,
                    lifecycleScope,
                    onError
                )

                dialog.show()
            } catch (e: Exception) {
                onError?.invoke("Error opening image: ${e.localizedMessage}")
            }
        }

        private fun loadImageManually(
            context: Context,
            uri: Uri,
            imageView: ImageView,
            progressBar: ProgressBar,
            lifecycleScope: LifecycleCoroutineScope,
            onError: ((String) -> Unit)? = null
        ) {
            progressBar.visibility = View.VISIBLE
            imageView.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(inputStream, null, options)

                    options.inSampleSize = calculateInSampleSize(
                        options,
                        context.resources.displayMetrics.widthPixels,
                        context.resources.displayMetrics.heightPixels
                    )
                    options.inJustDecodeBounds = false

                    val finalInputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(finalInputStream, null, options)
                    val orientation = getOrientation(context, uri)
                    val rotatedBitmap = bitmap?.let { rotateBitmap(it, orientation) }

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        rotatedBitmap?.let { imageView.setImageBitmap(it) }
                            ?: onError?.invoke("Failed to load image")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        onError?.invoke("Error loading image: ${e.localizedMessage}")
                    }
                }
            }
        }

        private fun getOrientation(context: Context, uri: Uri): Int {
            return try {
                context.contentResolver.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }
        }

        private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }

    class ZoomableImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : AppCompatImageView(context, attrs, defStyleAttr) {
        private var originalBitmap: Bitmap? = null

        private val matrix = Matrix()
        private val savedMatrix = Matrix()
        private val start = PointF()
        private val mid = PointF()
        private var oldDist = 1f
        private var mode = NONE
        private var currentScale = 0f
        private var minScale = 0.0f
        private var maxScale = 5f

        companion object {
            private const val NONE = 0
            private const val DRAG = 1
            private const val ZOOM = 2
        }

        private val gestureDetector: GestureDetector

        init {
            scaleType = ScaleType.MATRIX

            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    resetImagePositionAndScale()
                    return true
                }
            })
        }


        override fun setImageBitmap(bm: Bitmap?) {
            originalBitmap = bm
            post {
                // If bitmap is wider than tall, it might need rotation
                bm?.let {
                    if (it.width > it.height) {
                        val matrix = Matrix().apply {
                            postRotate(90f)
                            // Center the rotated image
                            postTranslate((width - it.height) / 2f, (height - it.width) / 2f)
                        }

                        val rotatedBitmap = Bitmap.createBitmap(
                            it, 0, 0, it.width, it.height, matrix, true
                        )

                        super.setImageBitmap(rotatedBitmap)
                    } else {
                        super.setImageBitmap(it)
                    }

                    resetImagePositionAndScale()
                }
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            // Attempt to set bitmap if it was previously set but view wasn't ready
            originalBitmap?.let { setImageBitmap(it) }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    start.set(event.x, event.y)
                    mode = DRAG
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix)
                        midPoint(mid, event)
                        mode = ZOOM
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    when (mode) {
                        DRAG -> {
                            matrix.set(savedMatrix)
                            matrix.postTranslate(event.x - start.x, event.y - start.y)
                        }
                        ZOOM -> {
                            val newDist = spacing(event)
                            if (newDist > 10f) {
                                val scale = newDist / oldDist
                                val newScale = currentScale * scale
                                if (newScale in minScale..maxScale) {
                                    matrix.set(savedMatrix)
                                    matrix.postScale(scale, scale, mid.x, mid.y)
                                    currentScale = newScale
                                }
                            }
                        }
                    }
                    imageMatrix = matrix
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
            }
            return true
        }

        private fun spacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return sqrt(x * x + y * y)
        }

        private fun midPoint(point: PointF, event: MotionEvent) {
            val x = event.getX(0) + event.getX(1)
            val y = event.getY(0) + event.getY(1)
            point.set(x / 2, y / 2)
        }

        private fun initializeImageScaleAndPosition() {
            val drawable = drawable ?: return

            val drawableWidth = drawable.intrinsicWidth.toFloat()
            val drawableHeight = drawable.intrinsicHeight.toFloat()

            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            val scaleX = viewWidth / drawableWidth
            val scaleY = viewHeight / drawableHeight

            // Choose the smaller scale to fit the image within the view
            val initialScale = minOf(scaleX, scaleY) * 0.9f // Slightly zoomed out

            // Center the image
            val dx = (viewWidth - drawableWidth * initialScale) / 2
            val dy = (viewHeight - drawableHeight * initialScale) / 2

            matrix.setScale(initialScale, initialScale)
            matrix.postTranslate(dx, dy)

            currentScale = initialScale
            imageMatrix = matrix
        }

        private fun resetImagePositionAndScale() {
            initializeImageScaleAndPosition()
            invalidate()
        }
    }
}
