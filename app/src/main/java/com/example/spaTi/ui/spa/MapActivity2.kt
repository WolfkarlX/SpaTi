package com.example.spaTi.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.spaTi.R
import com.example.spaTi.databinding.ActivityMap2Binding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMap2Binding
    private lateinit var googleMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var spaName: String? = ""
    private var spaImage: Int = R.drawable.spa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMap2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        spaName = intent.getStringExtra("spaName") ?: spaName
        spaImage = intent.getIntExtra("spaImage", R.drawable.spa)

        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Coordenadas inválidas.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("MapActivity2", "Coordenadas recibidas: Lat: $latitude, Lon: $longitude")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnBackToSpaDetail.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val spaLocation = LatLng(latitude, longitude)
        val customMarker = createCustomMarker(spaImage, spaName)

        googleMap.addMarker(
            MarkerOptions()
                .position(spaLocation)
                .title(spaName)
                .icon(customMarker)
        )


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spaLocation, 18f))
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun createCustomMarker(imageRes: Int, name: String?): BitmapDescriptor {
        val drawable: Drawable =
            ContextCompat.getDrawable(this, R.drawable.pin)!!
        val markerWidth = drawable.intrinsicWidth
        val markerHeight = drawable.intrinsicHeight

        val bitmap = Bitmap.createBitmap(markerWidth, markerHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)


        drawable.setBounds(0, 0, markerWidth, markerHeight)
        drawable.draw(canvas)

        if (!name.isNullOrEmpty()) {
            val paint = Paint().apply {
                color = ContextCompat.getColor(this@MapActivity2, R.color.black)
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            val textBounds = Rect()
            paint.getTextBounds(name, 0, name.length, textBounds)
            val textY = markerHeight - 20
            canvas.drawText(name, markerWidth / 2f, textY.toFloat(), paint)
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}