package com.example.spaTi.ui.map

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spaTi.R
import com.example.spaTi.databinding.ActivityMap2Binding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMap2Binding
    private lateinit var googleMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var spaName: String? = "Ubicación del Spa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMap2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener las coordenadas de la ubicación
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        // Verificar si las coordenadas son válidas
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Coordenadas inválidas.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("MapActivity2", "Coordenadas recibidas: Lat: $latitude, Lon: $longitude")

        // Obtener el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar el botón para regresar
        binding.btnBackToSpaDetail.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val spaLocation = LatLng(latitude, longitude)

        // Crear un pin verde usando el marcador por defecto
        val markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)

        // Crear el marcador y añadirlo al mapa
        googleMap.addMarker(
            MarkerOptions()
                .position(spaLocation)
                .title(spaName) // El título del marcador
                .snippet("Ubicación: $spaName") // El snippet muestra el nombre del spa
                .icon(markerIcon) // Usar el ícono verde predeterminado
        )


        // Mover la cámara para centrarla en la ubicación del spa
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spaLocation, 15f))

        // Habilitar los controles de zoom
        googleMap.uiSettings.isZoomControlsEnabled = true
    }
}
