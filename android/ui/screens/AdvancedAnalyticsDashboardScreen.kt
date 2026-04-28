package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.app.AnalyticsSnapshot
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarmOrange

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
            SectionHeader(title = "Insights", subtitle = "Analytics across your dining universe")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "At a Glance",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "🏨  ${snapshot.totalRestaurants} restaurants tracked",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "★  Avg rating: ${String.format("%.2f", snapshot.avgRating)} / 5",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "💬  ${snapshot.totalReviews} reviews analysed",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "📍  Top city: ${snapshot.topCity}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🍽️  Top cuisine: ${snapshot.topCuisine}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "For You", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Personalised picks based on your vibe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    recommendations.forEachIndexed { index, restaurant ->
                        Text(
                            text = "${index + 1}. ${restaurant.name} · ${restaurant.city}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Vibe Match Leaderboard", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Your top-matching restaurants right now",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (vibeLeaderboard.isEmpty()) {
                        Text(text = "No restaurants loaded yet.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        vibeLeaderboard.forEachIndexed { index, (restaurant, score) ->
                            val medals = listOf("🥇", "🥈", "🥉")
                            val pct = score.coerceIn(0f, 10f) / 10f
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${medals.getOrElse(index) { "#${index + 1}" }}  ${restaurant.name}",
                                        style = if (index == 0) MaterialTheme.typography.bodyMedium
                                               else MaterialTheme.typography.bodySmall,
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", score * 10)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = pct,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = SageGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hidden Gems Spotlight
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "💎 Hidden Gems", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Highly-rated spots you haven’t saved yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (hiddenGems.isEmpty()) {
                        Text(
                            text = "You may have found them all!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        hiddenGems.forEach { restaurant ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = restaurant.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(text = "${restaurant.cuisine} · ${restaurant.city}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                TextButton(onClick = { onRestaurantClick(restaurant) }) {
                                    Text(text = "View", color = SageGreen)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recently Explored
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "🔎 Recently Explored", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Jump back in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (recentlyViewed.isEmpty()) {
                        Text(text = "Nothing yet — start browsing restaurants!", style = MaterialTheme.typography.bodySmall)
                    } else {
                        recentlyViewed.forEach { restaurant ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = restaurant.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(text = "${restaurant.cuisine} · ${restaurant.city}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                TextButton(onClick = { onRestaurantClick(restaurant) }) {
                                    Text(text = "Revisit", color = SageGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
