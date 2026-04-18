package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.app.AnalyticsSnapshot

@Composable
fun AdvancedAnalyticsDashboardScreen(
    snapshot: AnalyticsSnapshot,
    recommendations: List<Restaurant>,
    scrapeStatus: String,
    onSimulateScrape: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Advanced Analytics Dashboard", fontWeight = FontWeight.Bold)
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
                    Text(text = "Real-Time Review Scraping", fontWeight = FontWeight.SemiBold)
                    Text(text = "Status: $scrapeStatus")
                    Button(onClick = onSimulateScrape) {
                        Text("Simulate Scrape Run")
                    }
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
    }
}
