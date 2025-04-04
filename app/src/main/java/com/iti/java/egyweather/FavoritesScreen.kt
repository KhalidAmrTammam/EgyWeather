package com.iti.java.egyweather

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iti.java.egyweather.Model.BOJO.FavoriteLocation

@Composable
fun FavoritesScreen(navController: NavController) {
    val activity = LocalActivity.current as MainActivity
    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(activity.repository)
    )

    val favorites by viewModel.favorites.collectAsState()

    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val lat = data.getDoubleExtra("lat", 0.0)
                val lon = data.getDoubleExtra("lon", 0.0)
                val name = data.getStringExtra("name") ?: ""
                viewModel.addFavorite(FavoriteLocation(name = name, lat = lat, lon = lon))
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { val intent = Intent(activity, AddLocationActivity::class.java)
                    startForResult.launch(intent) },
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Location")
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(favorites, key = { it.id }) { favorite ->
                FavoriteItem(
                    favorite = favorite,
                    onDelete = { viewModel.removeFavorite(it) },
                    onClick = {
                        navController.navigate("forecast/${favorite.lat}/${favorite.lon}")
                    }
                )
            }
        }
    }
}

@Composable
fun FavoriteItem(
    favorite: FavoriteLocation,
    onDelete: (Long) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(favorite.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${favorite.lat}, ${favorite.lon}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { onDelete(favorite.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}