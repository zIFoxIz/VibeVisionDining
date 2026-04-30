package com.example.vibevision.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.EmptyStateCard
import com.example.vibevision.ui.components.FilterChipRow
import com.example.vibevision.ui.components.OverlayPanel
import com.example.vibevision.ui.components.RestaurantCard
import com.example.vibevision.ui.components.RestaurantCardVariant
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarmOrange
import kotlinx.coroutines.launch


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
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember { derivedStateOf { resultsState.firstVisibleItemIndex > 3 } }
    val context = LocalContext.current
    var locationError by remember { mutableStateOf<String?>(null) }
    val hasActiveSearch = query.isNotBlank() || selectedCity != "All"

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchCurrentLocation(
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
        SectionHeader(title = "Discover", subtitle = "Search restaurants by name, city, or vibe")

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Name, city, or vibe…") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = SageGreen
                )
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Primary action row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSearchNow,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Search", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = {
                    val permissionState = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (permissionState == PackageManager.PERMISSION_GRANTED) {
                        fetchCurrentLocation(
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
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Near Me", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Secondary action row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onToggleOverlay,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = if (showFilterOverlay) " Hide Filters" else " Filters",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            OutlinedButton(
                onClick = onShowAllRestaurants,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Show All", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = if (restaurants.isEmpty()) "No results yet"
                   else "${restaurants.size} restaurant${if (restaurants.size == 1) "" else "s"} found",
            style = MaterialTheme.typography.labelLarge,
            color = SageGreen
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = SageGreen
            )
            Text(
                text = "Searching…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Rose.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Rose,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearError) {
                        Text("Dismiss", color = Rose)
                    }
                }
            }
        }

        if (locationError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmOrange.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = locationError ?: "",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmOrange
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

        if (showScrollToTop) {
            FloatingActionButton(
                onClick = { scope.launch { resultsState.animateScrollToItem(0) } },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = SageGreen,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Scroll to top",
                    tint = Color.White
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    onFound: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val tokenSource = CancellationTokenSource()

    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onFound(location.latitude, location.longitude)
            } else {
                fusedClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            onFound(lastLocation.latitude, lastLocation.longitude)
                        } else {
                            onError("Could not determine your location. Turn on device location and retry.")
                        }
                    }
                    .addOnFailureListener { error ->
                        onError(error.message ?: "Could not read last known location.")
                    }
            }
        }
        .addOnFailureListener { error ->
            onError(error.message ?: "Location request failed.")
        }
}
