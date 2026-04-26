package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.app.AnalyticsSnapshot

@Composable
fun AdvancedAnalyticsDashboardScreen(
    snapshot: AnalyticsSnapshot,
    recommendations: List<Restaurant>,
    vibeLeaderboard: List<Pair<Restaurant, Float>>,
    hiddenGems: List<Restaurant>,
    recentlyViewed: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Insights", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "KPI Snapshot", fontWeight = FontWeight.SemiBold)
                    Text(text = "Restaurants tracked: ${snapshot.totalRestaurants}")
                    Text(text = "Reviews tracked: ${snapshot.totalReviews}")
                    Text(text = "Average rating: ${String.format("%.2f", snapshot.avgRating)}")
                    Text(text = "Top city: ${snapshot.topCity}")
                    Text(text = "Top cuisine: ${snapshot.topCuisine}")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Personalized Restaurant Recommendations", fontWeight = FontWeight.SemiBold)
                    recommendations.forEachIndexed { index, restaurant ->
                        Text(text = "${index + 1}. ${restaurant.name} (${restaurant.city})")
                    }
                }
            }
        }

        // Vibe Match Leaderboard
        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Vibe Match Leaderboard", fontWeight = FontWeight.SemiBold)
                    Text(text = "Your top-matching restaurants right now", fontSize = 12.sp)
                    if (vibeLeaderboard.isEmpty()) {
                        Text(text = "No restaurants loaded yet.")
                    } else {
                        vibeLeaderboard.forEachIndexed { index, (restaurant, score) ->
                            val medals = listOf("1st", "2nd", "3rd")
                            val pct = score.coerceIn(0f, 10f) / 10f
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${medals.getOrElse(index) { "#${index + 1}" }}  ${restaurant.name}",
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", score * 10)}%",
                                        fontSize = 12.sp
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = pct,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hidden Gems Spotlight
        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Hidden Gems Spotlight", fontWeight = FontWeight.SemiBold)
                    Text(text = "Highly-rated spots you haven't saved yet", fontSize = 12.sp)
                    if (hiddenGems.isEmpty()) {
                        Text(text = "No hidden gems found — you may have already saved them all!")
                    } else {
                        hiddenGems.forEach { restaurant ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = restaurant.name, fontWeight = FontWeight.Medium)
                                    Text(text = "${restaurant.cuisine} • ${restaurant.city}", fontSize = 12.sp)
                                }
                                TextButton(onClick = { onRestaurantClick(restaurant) }) {
                                    Text(text = "View")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recently Explored
        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Recently Explored", fontWeight = FontWeight.SemiBold)
                    Text(text = "Jump back to a place you checked out", fontSize = 12.sp)
                    if (recentlyViewed.isEmpty()) {
                        Text(text = "Nothing explored yet — start browsing restaurants!")
                    } else {
                        recentlyViewed.forEach { restaurant ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = restaurant.name, fontWeight = FontWeight.Medium)
                                    Text(text = "${restaurant.cuisine} • ${restaurant.city}", fontSize = 12.sp)
                                }
                                TextButton(onClick = { onRestaurantClick(restaurant) }) {
                                    Text(text = "Revisit")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
