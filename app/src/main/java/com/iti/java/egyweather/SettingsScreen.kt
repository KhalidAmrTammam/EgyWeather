package com.iti.java.egyweather

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onLocationSourceChanged: (Boolean) -> Unit
) {
    val settingsState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Units Settings", style = MaterialTheme.typography.headlineSmall)
            Divider()
        }

        item {
            Text("Temperature Unit", modifier = Modifier.padding(vertical = 8.dp))
            Row {
                listOf("Celsius", "Fahrenheit", "Kelvin").forEach { unit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = settingsState.tempUnit == unit.lowercase(),
                            onClick = {
                                viewModel.updateSettings(
                                    settingsState.copy(tempUnit = unit.lowercase())
                                )
                            }
                        )
                        Text(unit)
                    }
                }
            }
        }

        item {
            Text("Wind Speed Unit", modifier = Modifier.padding(vertical = 8.dp))
            Row {
                listOf("m/s", "mph").forEach { unit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = settingsState.windUnit == unit,
                            onClick = {
                                viewModel.updateSettings(
                                    settingsState.copy(windUnit = unit)
                                )
                            }
                        )
                        Text(unit)
                    }
                }
            }
        }

        item {
            Text("Location Settings", style = MaterialTheme.typography.headlineSmall)
            Divider()
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.location_source))
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = settingsState.locationSource == "gps",
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settingsState.copy(locationSource = if (it) "gps" else "map")
                        )
                        onLocationSourceChanged(it)
                    }
                )
                Text(
                    if (settingsState.locationSource == "gps")
                        stringResource(R.string.gps)
                    else
                        stringResource(R.string.map)
                )
            }
        }

        item {
            Text("Language Settings", style = MaterialTheme.typography.headlineSmall)
            Divider()
        }

        item {
            Column {
                listOf("English", "Arabic", "System Default").forEach { lang ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(
                                    when (lang) {
                                        "English" -> "en"
                                        "Arabic" -> "ar"
                                        else -> "system"
                                    }
                                )
                            }
                    ) {
                        RadioButton(
                            selected = when {
                                lang == "System Default" -> settingsState.useSystemLanguage
                                else -> !settingsState.useSystemLanguage &&
                                        settingsState.language == lang.substring(0, 2).lowercase()
                            },
                            onClick = {
                                viewModel.setLanguage(
                                    when (lang) {
                                        "English" -> "en"
                                        "Arabic" -> "ar"
                                        else -> "system"
                                    }
                                )
                            }
                        )
                        Text(lang)
                    }
                }
            }
        }
    }
}