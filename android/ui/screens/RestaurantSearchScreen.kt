package com.example.vibevision.ui.screens

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.EmptyStateCard
import com.example.vibevision.ui.components.FilterChipRow
import com.example.vibevision.ui.components.OverlayPanel
import com.example.vibevision.ui.components.RestaurantCard
import com.example.vibevision.ui.components.RestaurantCardVariant
import com.example.vibevision.ui.components.SectionHeader
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
    showFilterOverlay: Boolean,
    onQueryChange: (String) -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onSetCity: (String) -> Unit,
    onToggleOverlay: () -> Unit,
    onToggleCuisine: (String) -> Unit,
    onTogglePrice: (Int) -> Unit,
    onToggleVibe: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val resultsState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        SectionHeader(title = "Restaurant Search", subtitle = "Filter by city, cuisine, vibe, and price")

        Text(text = "Multi-City Support")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Button(onClick = onToggleOverlay, modifier = Modifier.fillMaxWidth()) {
            Text(if (showFilterOverlay) "Hide Filter Overlay" else "Show Filter Overlay")
        }

        Text(
            text = "${restaurants.size} result(s) in $selectedCity",
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            state = resultsState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (restaurants.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No results found",
                        message = "Try changing city, query, or filters."
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
