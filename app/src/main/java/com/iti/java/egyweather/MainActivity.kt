package com.iti.java.egyweather

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iti.java.egyweather.Model.BOJO.ForecastItem
import com.iti.java.egyweather.Model.BOJO.ForecastResponse
import com.iti.java.egyweather.Model.BOJO.WeatherResponse
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDatabase
import com.iti.java.egyweather.Model.RemoteDataSource.RemoteDataSource
import com.iti.java.egyweather.Model.RemoteDataSource.RetrofitHelper
import com.iti.java.egyweather.Model.WeatherRepository
import com.iti.java.egyweather.ui.theme.EgyWeatherTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import kotlinx.datetime.Clock
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import android.Manifest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import android.content.Intent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.google.android.gms.maps.model.LatLng


class MainActivity : ComponentActivity() {
    private val placesClient by lazy { (application as MainApplication).placesClient }
    private var manualLocation by mutableStateOf<LatLng?>(null)
    private val locationService by lazy { LocationService(this) }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository(this))
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            fetchLocation()
        }
    }

    private val addLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val lat = data.getDoubleExtra("lat", 0.0)
                val lon = data.getDoubleExtra("lon", 0.0)
                viewModel.loadWeather(lat.toString(), lon.toString())
            }
        }
    }

    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(
            WeatherRepository(
                RemoteDataSource(RetrofitHelper.api),
                WeatherDatabase.getInstance(this).weatherDao(),
                this
            ),
            WorkManager.getInstance(this),
            settingsViewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("language", "system") ?: "system"
        if (lang != "system") {
            Locale.setDefault(Locale(lang))
        }

        createNotificationChannel()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uiState.collect { state ->
                    if (state.language != currentLanguage) {
                        //recreate()
                    }
                }
            }
        }

        setContent {
            MainContent(this)
        }
    }



    @Composable
    private fun MainContent(context: Context) {
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) Log.d("TAG", "Notification permission denied")
        }

        LaunchedEffect(Unit) {
            settingsViewModel.uiState.collect { settings ->
                when {
                    settings.useSystemLanguage -> {
                        val systemLang = Locale.getDefault().language
                        setAppLocale(context, systemLang)
                    }
                    else -> setAppLocale(context, settings.language)
                }
            }
        }

        EgyWeatherTheme {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LaunchedEffect(Unit) {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }

            val navController = rememberNavController()
            val settingsState by settingsViewModel.uiState.collectAsState()

            LaunchedEffect(settingsState.locationSource) {
                if (settingsState.locationSource == "gps") {
                    checkLocationPermissions()
                }
            }

            Scaffold(
                topBar = { WeatherAppBar() },
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) { padding ->
                NavigationHost(
                    navController = navController,
                    weatherViewModel = viewModel,
                    padding = padding,
                    placesClient = placesClient,
                    onLocationSourceChanged = { useGPS ->
                        if (useGPS) checkLocationPermissions()
                        else launchMapPicker()
                    }
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "weather_alerts_channel",
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Weather alert notifications"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            fetchLocation()
        }
    }

    private fun checkLocationSource() {
        when(settingsViewModel.uiState.value.locationSource) {
            "gps" -> checkLocationPermissions()
            "map" -> manualLocation?.let {
                viewModel.loadWeather(it.latitude.toString(), it.longitude.toString())
            } ?: launchMapPicker()
        }
    }

    private fun fetchLocation() {
        lifecycleScope.launch {
            if(settingsViewModel.uiState.value.locationSource == "gps") {  // Changed here
                locationService.getCurrentLocation()?.let {
                    viewModel.loadWeather(it.latitude.toString(), it.longitude.toString())
                }
            }
        }
    }

    fun launchMapPicker() {
        val intent = Intent(this, AddLocationActivity::class.java)
        addLocationLauncher.launch(intent)
    }

    private val currentLanguage: String
        get() = resources.configuration.locales[0]?.language ?: "en"
}

// Rest of your existing composables (WeatherScreen, NavigationHost, etc.) remain the same

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val state by viewModel.state.collectAsState()

    state.selectedItems.dailyDetail?.let { items ->
        DailyDetailScreen(
            forecastItems = items,
            onBack = { viewModel.clearDailyDetail() },
            viewModel = viewModel
        )
    } ?: run {
        MainWeatherScreen(viewModel)
    }

    state.selectedItems.hourly?.let { item ->
        ForecastDetailDialog(
            item = item,
            cityName = state.weatherData?.name ?: "Unknown City",
            onDismiss = { viewModel.clearSelection() }
        )
    }
}

@Composable
fun MainWeatherScreen(viewModel: WeatherViewModel) {
    val state by viewModel.state.collectAsState()
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )

    val dailyForecastGroups by remember(state.forecastData) {
        derivedStateOf {
            state.forecastData?.list
                ?.groupBy {
                    Instant.fromEpochSeconds(it.dt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                }
                ?.toSortedMap() ?: sortedMapOf()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .statusBarsPadding()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { CurrentWeatherSection(state.weatherData) }
            item { WeatherDetailsGrid(state.weatherData) }
            item { ThreeHourForecastSection(state.forecastData, viewModel) }
            item { FiveDayForecastSection(dailyForecastGroups, viewModel) }
        }

        if (state.isLoading) LoadingIndicator()
        state.errorMessage?.let { ErrorMessage(it) }
        state.selectedItems.hourly?.let {
            ForecastDetailDialog(
                item = it,
                cityName = state.weatherData?.name ?: "Unknown City",
                onDismiss = { viewModel.clearSelection() }  // Add this line
            )
        }
    }
}

// Removed duplicate LocalDate comparison extension (already handled by sortedMap)

@Composable
private fun CurrentWeatherSection(weather: WeatherResponse?) {
    weather?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${weather?.weather?.first()?.icon}@4x.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "${weather?.main?.temp?.toInt() ?: "--"}°C",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = weather?.name ?: "Unknown Location",
                    style = MaterialTheme.typography.headlineMedium
                )
                weather?.dt?.let { timestamp ->
                    val dateTime = Instant.fromEpochSeconds(timestamp)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    Text(
                        text = dateTime.date.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    Text(
                        text = dateTime.time.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    weather.weather.firstOrNull()?.let { weather ->
                        Text(
                            text = weather.description.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    } ?: run {
        Text("No weather data available", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun WeatherDetailsGrid(weather: WeatherResponse?) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.heightIn(max = 400.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(listOf(
            Triple(Icons.Default.WaterDrop, "Humidity", "${weather?.main?.humidity ?: "--"}%"),
            Triple(Icons.Default.Air, "Wind", "${weather?.wind?.speed ?: "--"} m/s"),
            Triple(Icons.Default.Speed, "Pressure", "${weather?.main?.pressure ?: "--"} hPa"),
            Triple(Icons.Default.Cloud, "Clouds", "${weather?.clouds?.all ?: "--"}%")
        )) { (icon, title, value) ->
            WeatherDetailItem(icon, title, value)
        }
    }
}

@Composable
private fun ThreeHourForecastSection(forecast: ForecastResponse?, viewModel: WeatherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "3-Hour Forecast",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            forecast?.list?.let { items ->
                items(items.take(8)) { item ->
                    ForecastItemView(item, viewModel)
                }
            }
        }
    }
}

@Composable
private fun FiveDayForecastSection(
    dailyForecastGroups: Map<LocalDate, List<ForecastItem>>,
    viewModel: WeatherViewModel
) {
    val systemTimeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTimeZone).date

    val filteredEntries = dailyForecastGroups.entries
        .sortedBy { it.key }
        .filter { it.key > today }
        .take(5)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "5-Day Forecast",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        filteredEntries.forEach { (date, items) ->
            DailyForecastItem(
                date = date,
                items = items,
                onClick = { viewModel.showDailyDetail(items) }
            )
        }
    }
}

// Reusable Components
@Composable
fun WeatherDetailItem(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ForecastItemView(item: ForecastItem, viewModel: WeatherViewModel) {
    val weather = item.weather.firstOrNull() ?: return
    val timeZone = TimeZone.currentSystemDefault()

    Card(
        modifier = Modifier
            .width(120.dp)
            .padding(4.dp)
            .clickable { viewModel.selectHourlyItem(item) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Instant.fromEpochSeconds(item.dt)
                    .toLocalDateTime(timeZone)
                    .time.toString().take(5),
                style = MaterialTheme.typography.bodyMedium
            )
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${weather.icon}.png",
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "${item.main.temp.toInt()}°C",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun DailyForecastItem(
    date: LocalDate,
    items: List<ForecastItem>,
    onClick: () -> Unit
) {
    val dailyAverage = remember(items) { items.map { it.main.temp }.average().toInt() }
    val dayName = remember(date) {
        date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                text = "$dailyAverage°C",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            AsyncImage(
                model = "https://openweathermap.org/img/wn/${items.first().weather.first().icon}.png",
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ForecastDetailDialog(
    item: ForecastItem,
    cityName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weather Details for $cityName") },
        text = {
            Column {
                WeatherDetailRow(Icons.Default.LocationCity, "City", cityName)
                WeatherDetailRow(Icons.Default.Thermostat, "Temp", "${item.main.temp.toInt()}°C")
                WeatherDetailRow(Icons.Default.DeviceThermostat, "Feels like", "${item.main.feelsLike.toInt()}°C")
                WeatherDetailRow(Icons.Default.WaterDrop, "Humidity", "${item.main.humidity}%")
                WeatherDetailRow(Icons.Default.Speed, "Pressure", "${item.main.pressure} hPa")
                WeatherDetailRow(Icons.Default.Air, "Wind", "${item.wind.speed} m/s")
                WeatherDetailRow(Icons.Default.Cloud, "Clouds", "${item.clouds.all}%")
                item.weather.firstOrNull()?.let { weather ->
                    WeatherDetailRow(Icons.Default.Info, "Condition", weather.description.replaceFirstChar { it.uppercase() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
@Composable
fun DailyDetailScreen(
    forecastItems: List<ForecastItem>,
    onBack: () -> Unit,
    viewModel: WeatherViewModel
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Card(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .clickable { onBack() },
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp) )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back to Forecast",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
        forecastItems.firstOrNull()?.let { firstItem ->
            val date = Instant.fromEpochSeconds(firstItem.dt)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            Text(
                text = date.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(forecastItems) { item ->
                DailyDetailItem(
                    item = item,
                    viewModel = viewModel
                )
            }
        }

        forecastItems.firstOrNull()?.let { firstItem ->
            DailyDetailWeatherGrid(firstItem)
        }
    }
}

@Composable
private fun DailyDetailItem(item: ForecastItem, viewModel: WeatherViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { viewModel.selectHourlyItem(item) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = Instant.fromEpochSeconds(item.dt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .time.toString().take(5),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = item.weather.first().description.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AsyncImage(
                model = "https://openweathermap.org/img/wn/${item.weather.first().icon}.png",
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "${item.main.temp.toInt()}°C",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun WeatherDetailGrid(item: ForecastItem) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(16.dp)
    ) {
        items(listOf(
            Triple(Icons.Default.WaterDrop, "Humidity", "${item.main.humidity}%"),
            Triple(Icons.Default.Air, "Wind", "${item.wind.speed} m/s"),
            Triple(Icons.Default.Speed, "Pressure", "${item.main.pressure} hPa"),
            Triple(Icons.Default.Cloud, "Clouds", "${item.clouds.all}%")
        )) { (icon, title, value) ->
            WeatherDetailItem(icon, title, value)
        }
    }
}
@Composable
private fun DailyDetailWeatherGrid(item: ForecastItem) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(listOf(
            Triple(Icons.Default.WaterDrop, "Humidity", "${item.main.humidity}%"),
            Triple(Icons.Default.Air, "Wind", "${item.wind.speed} m/s"),
            Triple(Icons.Default.Speed, "Pressure", "${item.main.pressure} hPa"),
            Triple(Icons.Default.Cloud, "Clouds", "${item.clouds.all}%")
        )) { (icon, title, value) ->
            WeatherDetailItem(icon, title, value)
        }
    }
}
@Composable
private fun WeatherDetailRow(icon: ImageVector, title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title: $value",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    weatherViewModel: WeatherViewModel,
    padding: PaddingValues,
    placesClient: PlacesClient,
    onLocationSourceChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Home.route) {
            WeatherScreen(viewModel = weatherViewModel)
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    SettingsRepository(application)
                )
            )

            SettingsScreen(
                viewModel = settingsViewModel,
                onLocationSourceChanged = onLocationSourceChanged
            )
        }
        composable(Screen.Forecast.route) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
            ForecastScreen(
                lat = lat,
                lon = lon,
                viewModel = weatherViewModel
            )
        }
        composable(Screen.Alerts.route) {
            val alertsViewModel: AlertsViewModel = viewModel(
                factory = AlertsViewModelFactory(
                    repository = application.repository,
                    application = application
                )
            )
            AlertsScreen(
                navController = navController,
                viewModel = alertsViewModel
            )
        }

        composable(Screen.AlertSettings.route) {
            val alertsViewModel: AlertsViewModel = viewModel(
                factory = AlertsViewModelFactory(
                    repository = application.repository,
                    application = application
                )
            )
            AlertSettingScreen(
                navController = navController,
                viewModel = alertsViewModel,
                placesClient = placesClient
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites") },
            selected = currentRoute == Screen.Favorites.route,
            onClick = { navController.navigate(Screen.Favorites.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts") },
            selected = currentRoute == Screen.Alerts.route,
            onClick = { navController.navigate(Screen.Alerts.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { navController.navigate(Screen.Settings.route) }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "EgyWeather",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
@Composable
private fun TemperatureText(temp: Double, unit: String) {
    val unitString = when(unit) {
        "celsius" -> stringResource(R.string.celsius)
        "fahrenheit" -> stringResource(R.string.fahrenheit)
        else -> stringResource(R.string.kelvin)
    }
    Text("${temp.toInt()}$unitString")
}

@Composable
private fun WindSpeedText(speed: Double, unit: String) {
    val unitString = when(unit) {
        "m/s" -> stringResource(R.string.ms)
        else -> stringResource(R.string.mph)
    }
    Text("${"%.1f".format(speed)} $unitString")
}