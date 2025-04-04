package com.iti.java.egyweather

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun PlacesAutocomplete(
    placesClient: PlacesClient,
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchQuery)
                .build()

            try {
                val response = withContext(Dispatchers.IO) {
                    placesClient.findAutocompletePredictions(request).await()
                }
                predictions = response.autocompletePredictions
            } catch (e: Exception) {
                predictions = emptyList()
            }
        }
    }

    Column(modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search location") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
            items(predictions) { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val placeRequest = FetchPlaceRequest.builder(
                                prediction.placeId,
                                listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                            ).build()

                            placesClient.fetchPlace(placeRequest)
                                .addOnSuccessListener { response ->
                                    onPlaceSelected(response.place)
                                }
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}