package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.EmptyStateCard
import com.example.vibevision.ui.components.RestaurantCard
import com.example.vibevision.ui.components.SectionHeader

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

        if (showFilterOverlay) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Filter Overlay", fontWeight = FontWeight.SemiBold)

                    Text(text = "Cuisine Filters")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableCuisines) { cuisine ->
                            AssistChip(
                                onClick = { onToggleCuisine(cuisine) },
                                label = { Text(cuisine) },
                                leadingIcon = { Text(if (selectedCuisines.contains(cuisine)) "*" else "") }
                            )
                        }
                    }

                    Text(text = "Price Filters")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availablePrices) { price ->
                            AssistChip(
                                onClick = { onTogglePrice(price) },
                                label = { Text("$".repeat(price)) },
                                leadingIcon = { Text(if (selectedPrices.contains(price)) "*" else "") }
                            )
                        }
                    }

                    Text(text = "Vibe Filters")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableVibes) { vibe ->
                            AssistChip(
                                onClick = { onToggleVibe(vibe) },
                                label = { Text(vibe) },
                                leadingIcon = { Text(if (selectedVibes.contains(vibe)) "*" else "") }
                            )
                        }
                    }

                    Button(onClick = onClearFilters) {
                        Text("Clear All Filters")
                    }
                }
            }
        }

        Text(
            text = "${restaurants.size} result(s) in $selectedCity",
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
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
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }
    }
}
