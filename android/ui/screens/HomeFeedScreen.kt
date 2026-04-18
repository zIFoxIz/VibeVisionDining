package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
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
import com.example.vibevision.ui.components.MetricCard
import com.example.vibevision.ui.components.RestaurantCard
import com.example.vibevision.ui.components.SectionHeader

@Composable
fun HomeFeedScreen(
    restaurants: List<Restaurant>,
    favorites: List<Restaurant>,
    recentlyViewed: List<Restaurant>,
    favoriteIds: Set<String>,
    recommendations: List<Restaurant>,
    isOfflineMode: Boolean,
    aiSummary: String,
    onRestaurantClick: (Restaurant) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    val topPicks = restaurants.take(3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isOfflineMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Offline Mode", fontWeight = FontWeight.SemiBold)
                        Text(text = "Live network features are disabled. Local analysis and cached data remain available.")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "AI Sentiment Summary", fontWeight = FontWeight.SemiBold)
                    Text(text = aiSummary)
                }
            }
        }

        item {
            SectionHeader(title = "Home Feed", subtitle = "Discover, compare, and save your next restaurant")
        }

        item {
            MetricCard(
                title = "Today at a Glance",
                value = "${restaurants.size} restaurants • ${favorites.size} favorites • ${recentlyViewed.size} recently viewed"
            )
        }

        if (recommendations.isNotEmpty()) {
            item {
                Text(text = "Personalized Restaurant Recommendations", fontWeight = FontWeight.SemiBold)
            }
            items(recommendations) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }

        if (favorites.isNotEmpty()) {
            item {
                Text(text = "Favorites List", fontWeight = FontWeight.SemiBold)
            }
            items(favorites) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }

        if (recentlyViewed.isNotEmpty()) {
            item {
                Text(text = "Recently Viewed Restaurants", fontWeight = FontWeight.SemiBold)
            }
            items(recentlyViewed) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }

        if (topPicks.isNotEmpty()) {
            item {
                Text(text = "Top Picks", fontWeight = FontWeight.SemiBold)
            }
            items(topPicks) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle
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

        item {
            Text(
                text = "End of feed",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
