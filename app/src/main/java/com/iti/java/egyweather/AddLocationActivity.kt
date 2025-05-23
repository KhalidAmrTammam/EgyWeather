package com.iti.java.egyweather

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import java.util.Locale

class AddLocationActivity : ComponentActivity() {
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyD3yetxMtpwjWOXNbDcSX8-G6mMl0v0lV4")
        }
        placesClient = Places.createClient(this)

        setContent {
            LocationPickerScreen(
                placesClient = placesClient,
                onLocationSelected = { lat, lon, name ->
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("lat", lat)
                        putExtra("lon", lon)
                        putExtra("name", name)
                    })
                    finish()
                }
            )
        }
    }

}

@Composable
fun LocationPickerScreen(
    placesClient: PlacesClient,
    onLocationSelected: (Double, Double, String) -> Unit
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }

    Column(Modifier.fillMaxSize()) {
        PlacesAutocomplete(
            placesClient = placesClient,
            onPlaceSelected = { place ->
                place.latLng?.let { latLng ->
                    selectedPosition = latLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    onLocationSelected(latLng.latitude, latLng.longitude, place.name)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedPosition = latLng
                val address = getAddressFromLatLng(context, latLng)
                onLocationSelected(latLng.latitude, latLng.longitude, address)
            }
        ) {
            selectedPosition?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = "Selected Location"
                )
            }
        }
    }
}

fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
    return try {
        Geocoder(context, Locale.getDefault()).getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        )?.firstOrNull()?.let { address ->
            "${address.locality ?: ""} ${address.countryName ?: ""}".trim()
        } ?: "Custom Location"
    } catch (e: Exception) {
        "Custom Location"
    }
}