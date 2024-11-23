package com.example.spaTi.ui.SpaProfile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.spaTi.R
import com.example.spaTi.databinding.ActivityMap3Binding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.Locale

class MapActivity3 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMap3Binding
    private lateinit var googleMap: GoogleMap
    private var spaLocation: String? = null
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMap3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        spaLocation = intent.getStringExtra("spaLocation")
        if (spaLocation == null || spaLocation!!.isEmpty()) {
            Toast.makeText(this, "No se recibió ninguna ubicación del spa", Toast.LENGTH_SHORT).show()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocationBtn.setOnClickListener {
            selectedLocation?.let {
                val plainAddress = getAddressFromCoordinates(it)
                if (plainAddress != null) {
                    val resultIntent = Intent().apply {
                        putExtra("newLocation", plainAddress)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Selecciona una ubicación primero", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        spaLocation?.let {
            val coordinates = parseLocationString(it) ?: getCoordinatesFromAddress(it)
            if (coordinates != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 18f))
                googleMap.addMarker(
                    MarkerOptions()
                        .position(coordinates)
                        .title("Ubicación actual del spa")
                        .icon(getCustomPin())
                )
            } else {
                Toast.makeText(this, "No se pudo obtener las coordenadas de la ubicación", Toast.LENGTH_SHORT).show()
            }
        }

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Nueva ubicación seleccionada")
                    .icon(getCustomPin())
            )
            selectedLocation = latLng
        }
    }

    private fun getCustomPin(): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, R.drawable.pin)!!
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getCoordinatesFromAddress(address: String): LatLng? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                LatLng(addresses[0].latitude, addresses[0].longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLocationString(location: String): LatLng? {
        return try {
            val parts = location.replace("Lat:", "").replace("Lon:", "").split(",")
            val latitude = parts[0].trim().toDouble()
            val longitude = parts[1].trim().toDouble()
            LatLng(latitude, longitude)
        } catch (e: Exception) {
            null
        }
    }

    private fun getAddressFromCoordinates(latLng: LatLng): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
