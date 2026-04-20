package com.example.vibevision.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.EmptyStateCard
import com.example.vibevision.ui.components.FilterChipRow
import com.example.vibevision.ui.components.OverlayPanel
import com.example.vibevision.ui.components.RestaurantCard
import com.example.vibevision.ui.components.RestaurantCardVariant
import com.example.vibevision.ui.components.SectionHeader
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun RestaurantSearchScreen(
    query: String,
    restaurants: List<Restaurant>,
    selectedCity: String,
    availableCities: List<String>,
    availableCuisines: List<String>,
    availablePrices: List<Int>,
    availableVibes: List<String>,
    favoriteIds: Set<String>,
    selectedCuisines: Set<String>,
    selectedPrices: Set<Int>,
    selectedVibes: Set<String>,
    isLoading: Boolean,
    errorMessage: String?,
    showFilterOverlay: Boolean,
    onQueryChange: (String) -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onSetCity: (String) -> Unit,
    onSearchNow: () -> Unit,
    onSearchNearMe: (Double, Double) -> Unit,
    onClearError: () -> Unit,
    onToggleOverlay: () -> Unit,
    onToggleCuisine: (String) -> Unit,
    onTogglePrice: (Int) -> Unit,
    onToggleVibe: (String) -> Unit,
    onClearFilters: () -> Unit,
    onShowAllRestaurants: () -> Unit
) {
    val resultsState = rememberLazyListState()
    val cityChipsState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var locationError by remember { mutableStateOf<String?>(null) }
    val hasActiveSearch = query.isNotBlank() || selectedCity != "All"

    LaunchedEffect(selectedCity, availableCities) {
        val selectedIndex = availableCities.indexOfFirst { it.equals(selectedCity, ignoreCase = true) }
        if (selectedIndex >= 0) {
            cityChipsState.animateScrollToItem(max(0, selectedIndex - 1))
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLastKnownLocation(
                context = context,
                onFound = { lat, lng ->
                    locationError = null
                    onSearchNearMe(lat, lng)
                },
                onError = { message ->
                    locationError = message
                }
            )
        } else {
            locationError = "Location permission denied."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        SectionHeader(title = "Restaurant Search", subtitle = "Filter by city, cuisine, vibe, and price")

        Text(text = "Multi-City Support")
        LazyRow(
            state = cityChipsState,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableCities) { city ->
                AssistChip(
                    onClick = { onSetCity(city) },
                    label = { Text(city) },
                    leadingIcon = { Text(if (selectedCity == city) "*" else "") }
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name, cuisine, or vibe") }
        )

        Button(onClick = onSearchNow, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Search Restaurants")
        }

        Button(
            onClick = {
                val permissionState = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (permissionState == PackageManager.PERMISSION_GRANTED) {
                    fetchLastKnownLocation(
                        context = context,
                        onFound = { lat, lng ->
                            locationError = null
                            onSearchNearMe(lat, lng)
                        },
                        onError = { message ->
                            locationError = message
                        }
                    )
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restaurants Near You")
        }

        Button(onClick = onToggleOverlay, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            Text(if (showFilterOverlay) "Hide Filter Overlay" else "Show Filter Overlay")
        }

        Button(onClick = onShowAllRestaurants, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Show All Restaurants")
        }

        Text(
            text = "${restaurants.size} result(s) in $selectedCity",
            fontWeight = FontWeight.SemiBold
        )

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(text = "Searching live restaurants...")
        }

        if (errorMessage != null) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = errorMessage)
                    Button(onClick = onClearError) {
                        Text("Dismiss")
                    }
                }
            }
        }

        if (locationError != null) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Text(
                    text = locationError ?: "",
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        LazyColumn(
            state = resultsState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (restaurants.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No results found",
                        message = if (hasActiveSearch) {
                            "Try a broader name/city search or tap Show All Restaurants."
                        } else {
                            "Tap Search Restaurants to load results."
                        }
                    )
                }
            }

            items(restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle,
                    variant = RestaurantCardVariant.DEFAULT
                )
            }
        }
    }

        if (showFilterOverlay) {
            OverlayPanel(
                title = "Filter Overlay",
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
            ) {
                FilterChipRow(
                    title = "Cuisine Filters",
                    options = availableCuisines,
                    selected = selectedCuisines,
                    onToggle = onToggleCuisine
                )

                FilterChipRow(
                    title = "Price Filters",
                    options = availablePrices.map { "$".repeat(it) },
                    selected = selectedPrices.map { "$".repeat(it) }.toSet(),
                    onToggle = { label -> onTogglePrice(label.length) }
                )

                FilterChipRow(
                    title = "Vibe Filters",
                    options = availableVibes,
                    selected = selectedVibes,
                    onToggle = onToggleVibe
                )

                Button(onClick = onClearFilters, modifier = Modifier.fillMaxWidth()) {
                    Text("Clear All Filters")
                }

                Button(onClick = onToggleOverlay, modifier = Modifier.fillMaxWidth()) {
                    Text("Close Overlay")
                }
            }
        }

        if (resultsState.firstVisibleItemIndex > 3) {
            FloatingActionButton(
                onClick = { scope.launch { resultsState.animateScrollToItem(0) } },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Text("Top")
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLastKnownLocation(
    context: Context,
    onFound: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (manager == null) {
        onError("Location service is unavailable.")
        return
    }

    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    )

    val best = providers
        .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull(Location::getTime)

    if (best == null) {
        onError("Could not determine your location yet. Try moving the emulator location and retry.")
    } else {
        onFound(best.latitude, best.longitude)
    }
}
