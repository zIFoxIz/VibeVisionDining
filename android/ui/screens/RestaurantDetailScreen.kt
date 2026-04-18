package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.components.DishCard
import com.example.vibevision.ui.components.EmotionHeatmap

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    vibePreferences: List<VibePreference>
) {
    val vibeMatchScore = calculateVibeMatch(restaurant.vibeTags, vibePreferences)

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
    }
}

private fun calculateVibeMatch(tags: List<String>, prefs: List<VibePreference>): Float {
    val enabled = prefs.filter { it.enabled }.map { it.vibe }.toSet()
    if (enabled.isEmpty()) return 0f
    val overlap = tags.count { enabled.contains(it) }
    return overlap.toFloat() / enabled.size.toFloat()
}
