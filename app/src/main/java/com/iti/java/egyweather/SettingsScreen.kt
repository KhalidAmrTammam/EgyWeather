package com.iti.java.egyweather

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = SettingsViewModel(SettingsRepository())) {
    val settingsState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = settingsState.celsius,
                onCheckedChange = { viewModel.updateSettings(settingsState.copy(celsius = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Celsius", style = MaterialTheme.typography.bodyLarge)
        }
    }
}