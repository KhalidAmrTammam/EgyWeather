package com.iti.java.egyweather

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Calendar

@Composable
fun AlertSettingScreen(
    navController: NavController,
    viewModel: AlertsViewModel,
    placesClient: PlacesClient
) {
    var duration by remember { mutableStateOf("15") }
    val context = LocalContext.current
    val calendar = remember { mutableStateOf(Calendar.getInstance()) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = { showDatePicker(context, calendar) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Select Date")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { showTimePicker(context, calendar) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Select Time")
            }
        }

        Text("Select Location:", modifier = Modifier.padding(top = 16.dp))
        PlacesAutocomplete(
            placesClient = placesClient,
            onPlaceSelected = { place ->
                selectedLocation = place.latLng?.let { LatLng(it.latitude, it.longitude) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                selectedLocation?.let { location ->
                    viewModel.addAlert(
                        lat = location.latitude,
                        lon = location.longitude,
                        time = calendar.value.timeInMillis,
                        type = "NOTIFICATION", // Hardcoded since we only have notifications
                        duration = duration.toIntOrNull() ?: 15
                    )
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            enabled = selectedLocation != null
        ) {
            Text("Save Weather Alert")
        }
    }
}

private fun showDatePicker(context: Context, calendar: MutableState<Calendar>) {
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.value.set(year, month, day)
        },
        calendar.value.get(Calendar.YEAR),
        calendar.value.get(Calendar.MONTH),
        calendar.value.get(Calendar.DAY_OF_MONTH))
    datePicker.show()
}

private fun showTimePicker(context: Context, calendar: MutableState<Calendar>) {
    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.value.set(Calendar.HOUR_OF_DAY, hour)
            calendar.value.set(Calendar.MINUTE, minute)
        },
        calendar.value.get(Calendar.HOUR_OF_DAY),
        calendar.value.get(Calendar.MINUTE),
        true)
    timePicker.show()
}