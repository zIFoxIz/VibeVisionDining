package com.example.vibevision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.BrandLogoMark
import com.example.vibevision.ui.components.MetricCard
import com.example.vibevision.ui.components.RestaurantCard
import com.example.vibevision.ui.components.RestaurantCardVariant
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.theme.InkBlue
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarmOrange
import kotlinx.coroutines.launch

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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BrandLogoMark()
            }

        if (isOfflineMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmOrange.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Offline Mode",
                                style = MaterialTheme.typography.labelLarge,
                                color = WarmOrange
                            )
                            Text(
                                text = "Live features paused — cached data is available.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(InkBlue, SageGreen)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🧠 AI Sentiment Summary",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = aiSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        item {
            SectionHeader(title = "Discover", subtitle = "Find and save your next great meal")
        }

        item {
            MetricCard(
                title = "Today at a Glance",
                value = "${restaurants.size} restaurants • ${favorites.size} favorites • ${recentlyViewed.size} recently viewed"
            )
        }

        if (recommendations.isNotEmpty()) {
            item {
                SectionHeader(title = "For You", subtitle = "Matched to your vibe preferences")
            }
            items(recommendations) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle,
                    variant = RestaurantCardVariant.FEATURED
                )
            }
        }

        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(title = "Favorites", subtitle = "Places you’ve saved")
            }
            items(favorites) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = onRestaurantClick,
                    isFavorite = favoriteIds.contains(restaurant.id),
                    onFavoriteToggle = onFavoriteToggle,
                    variant = RestaurantCardVariant.COMPACT
                )
            }
        }

        if (recentlyViewed.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Viewed", subtitle = "Pick up where you left off")
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
                SectionHeader(title = "Top Picks", subtitle = "Highest rated in your area")
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "—  You’ve seen it all  —",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (listState.firstVisibleItemIndex > 2) {
            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }

        if (listState.firstVisibleItemIndex > 2) {
            FloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
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
