package com.iti.java.egyweather

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val alerts by viewModel.weatherAlerts.collectAsState()

    LazyColumn {
        items(alerts) { alert ->
            AlertItem(alert = alert.toString())
        }
    }
}
@Composable
fun AlertItem(alert: String) {
    Card(modifier = Modifier.padding(8.dp)) {
        Text(text = alert, modifier = Modifier.padding(16.dp))
    }

}