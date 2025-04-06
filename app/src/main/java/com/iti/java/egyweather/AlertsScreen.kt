package com.iti.java.egyweather

import android.text.format.DateUtils.formatDateTime
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkManager
import com.iti.java.egyweather.Model.BOJO.WeatherAlert
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AlertsScreen(navController: NavController, viewModel: AlertsViewModel) {
    val alerts by viewModel.alerts.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AlertSettings.route) },
                icon = { Icon(Icons.Default.Add, "Add Alert") },
                text = { Text("New Alert") }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(alerts) { alert ->
                AlertItem(alert, viewModel)
            }
        }
    }
}

@Composable
fun AlertItem(alert: WeatherAlert, viewModel: AlertsViewModel) {
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(alert.locationName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Duration: ${alert.durationMinutes} mins • ${alert.alertType}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        Instant.fromEpochMilliseconds(alert.alertTime)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            IconButton(
                onClick = { viewModel.deleteAlert(alert.id) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}