package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.components.DishCard
import com.example.vibevision.ui.components.EmotionHeatmap

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    vibePreferences: List<VibePreference>,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onShareRestaurantCard: () -> Unit
) {
    val vibeMatchScore = calculateVibeMatch(restaurant.vibeTags, vibePreferences)
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOption by remember { mutableStateOf("Highest") }
    val filteredReviews = filterAndSortReviews(restaurant.reviews, selectedCategory, sortOption)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Restaurant Detail", fontWeight = FontWeight.Bold)
            Text(text = restaurant.name, fontWeight = FontWeight.SemiBold)
            Text(text = "${restaurant.cuisine} • ${"$".repeat(restaurant.priceLevel)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onFavoriteToggle) {
                    Text(if (isFavorite) "Remove Favorite" else "Add Favorite")
                }
                Button(onClick = onShareRestaurantCard) {
                    Text("Share Restaurant Card")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Restaurant Photo Gallery", fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(restaurant.photoLabels) { photo ->
                            Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    Text(text = photo)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Menu Preview Section", fontWeight = FontWeight.SemiBold)
                    restaurant.menuPreview.forEach { item ->
                        Text(text = "- $item")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Vibe Match System", fontWeight = FontWeight.SemiBold)
                    Text(text = "Current match: ${String.format("%.0f", vibeMatchScore * 100)}%")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "AI Sentiment Summary", fontWeight = FontWeight.SemiBold)
                    val summary = "Most reviews are positive for ${restaurant.name}, with strongest sentiment around ${restaurant.dishSentiments.firstOrNull()?.dishName ?: "top dishes"}."
                    Text(text = summary)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Emotion Heatmap", fontWeight = FontWeight.SemiBold)
                    EmotionHeatmap(
                        mapOf(
                            "joy" to (0.55f + vibeMatchScore * 0.3f),
                            "calm" to 0.45f,
                            "neutral" to 0.25f,
                            "frustration" to (0.2f - vibeMatchScore * 0.1f).coerceAtLeast(0.05f),
                            "anger" to 0.1f
                        )
                    )
                }
            }
        }

        item {
            Text(text = "Dish Sentiment Breakdown", fontWeight = FontWeight.SemiBold)
        }

        items(restaurant.dishSentiments) { dish ->
            DishCard(dish = dish)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Dish Rating Comparison", fontWeight = FontWeight.SemiBold)
                    val maxPositive = restaurant.dishSentiments.maxOfOrNull { it.positive }?.coerceAtLeast(1) ?: 1
                    restaurant.dishSentiments.forEach { dish ->
                        val ratio = dish.positive.toFloat() / maxPositive.toFloat()
                        Text(text = dish.dishName)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                                .height(12.dp)
                                .padding(bottom = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().height(12.dp)) {}
                        }
                        Text(text = "Positive votes: ${dish.positive}")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Review Sorting Options", fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Highest", "Lowest")) { option ->
                            AssistChip(
                                onClick = { sortOption = option },
                                label = { Text(option) },
                                leadingIcon = { Text(if (sortOption == option) "*" else "") }
                            )
                        }
                    }

                    Text(text = "Review Category Filters", fontWeight = FontWeight.SemiBold)
                    val categories = listOf("All") + ReviewCategory.entries.map { it.name.lowercase().replaceFirstChar { c -> c.titlecase() } }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            AssistChip(
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                leadingIcon = { Text(if (selectedCategory == category) "*" else "") }
                            )
                        }
                    }

                    filteredReviews.forEach { review ->
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "${review.rating}/5 • ${review.category.name}", fontWeight = FontWeight.SemiBold)
                                Text(text = review.text)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Restaurant Map Integration (placeholder)", fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .height(120.dp)
                        ) {
                            Text(text = "Map placeholder for ${restaurant.name}", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

private fun filterAndSortReviews(reviews: List<Review>, category: String, sort: String): List<Review> {
    val filtered = if (category == "All") {
        reviews
    } else {
        reviews.filter { it.category.name.equals(category, ignoreCase = true) }
    }

    return when (sort) {
        "Lowest" -> filtered.sortedBy { it.rating }
        else -> filtered.sortedByDescending { it.rating }
    }
}

private fun calculateVibeMatch(tags: List<String>, prefs: List<VibePreference>): Float {
    val enabled = prefs.filter { it.enabled }.map { it.vibe }.toSet()
    if (enabled.isEmpty()) return 0f
    val overlap = tags.count { enabled.contains(it) }
    return overlap.toFloat() / enabled.size.toFloat()
}
