package com.example.vibevision.ui.app

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vibevision.ml.SentimentAnalyzer
import com.example.vibevision.ui.SentimentAnalysisScreen
import com.example.vibevision.ui.SentimentViewModel
import com.example.vibevision.ui.screens.BasicProfileScreen
import com.example.vibevision.ui.screens.HomeFeedScreen
import com.example.vibevision.ui.screens.RestaurantDetailScreen
import com.example.vibevision.ui.screens.RestaurantSearchScreen

@Composable
fun VibeVisionApp(
    analyzer: SentimentAnalyzer,
    appViewModel: AppViewModel = viewModel()
) {
    val state by appViewModel.uiState.collectAsStateWithLifecycle()
    val sentimentViewModel = remember(analyzer) { SentimentViewModel(analyzer) }
    val context = LocalContext.current

    MaterialTheme(colorScheme = if (state.isDarkMode) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavItem("Home", state.route == AppRoute.HOME) { appViewModel.navigate(AppRoute.HOME) }
                    NavItem("Search", state.route == AppRoute.SEARCH) { appViewModel.navigate(AppRoute.SEARCH) }
                    NavItem("Analyze", state.route == AppRoute.ANALYZER) { appViewModel.navigate(AppRoute.ANALYZER) }
                    NavItem("Profile", state.route == AppRoute.PROFILE) { appViewModel.navigate(AppRoute.PROFILE) }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (state.route) {
                    AppRoute.HOME -> HomeFeedScreen(
                        restaurants = state.restaurants,
                        favorites = appViewModel.favoriteRestaurants(),
                        recentlyViewed = appViewModel.recentlyViewedRestaurants(),
                        favoriteIds = state.favoriteRestaurantIds,
                        aiSummary = "Overall trend is positive, with strongest sentiment around food quality and service.",
                        onRestaurantClick = appViewModel::openRestaurantDetail,
                        onFavoriteToggle = appViewModel::toggleFavorite
                    )

                    AppRoute.SEARCH -> RestaurantSearchScreen(
                        query = state.searchQuery,
                        restaurants = appViewModel.filteredRestaurants(),
                        availableCuisines = appViewModel.allCuisines(),
                        availablePrices = appViewModel.allPriceLevels(),
                        availableVibes = appViewModel.allVibes(),
                        selectedCuisines = state.selectedCuisineFilters,
                        selectedPrices = state.selectedPriceFilters,
                        selectedVibes = state.selectedVibeFilters,
                        showFilterOverlay = state.showFilterOverlay,
                        onQueryChange = appViewModel::updateSearchQuery,
                        onRestaurantClick = appViewModel::openRestaurantDetail,
                        onToggleOverlay = { appViewModel.setFilterOverlayVisible(!state.showFilterOverlay) },
                        onToggleCuisine = appViewModel::toggleCuisineFilter,
                        onTogglePrice = appViewModel::togglePriceFilter,
                        onToggleVibe = appViewModel::toggleVibeFilter,
                        onClearFilters = appViewModel::clearAllFilters
                    )

                    AppRoute.ANALYZER -> SentimentAnalysisScreen(viewModel = sentimentViewModel)

                    AppRoute.PROFILE -> BasicProfileScreen(
                        preferences = state.vibePreferences,
                        isDarkMode = state.isDarkMode,
                        onToggle = appViewModel::toggleVibe,
                        onDarkModeToggle = appViewModel::setDarkMode
                    )

                    AppRoute.DETAIL -> state.selectedRestaurant?.let { restaurant ->
                        RestaurantDetailScreen(
                            restaurant = restaurant,
                            vibePreferences = state.vibePreferences,
                            isFavorite = state.favoriteRestaurantIds.contains(restaurant.id),
                            onFavoriteToggle = { appViewModel.toggleFavorite(restaurant.id) },
                            onShareRestaurantCard = {
                                val text = "${restaurant.name} • ${restaurant.cuisine} • vibe: ${restaurant.vibeTags.joinToString()}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share restaurant"))
                            }
                        )
                    } ?: HomeFeedScreen(
                        restaurants = state.restaurants,
                        favorites = appViewModel.favoriteRestaurants(),
                        recentlyViewed = appViewModel.recentlyViewedRestaurants(),
                        favoriteIds = state.favoriteRestaurantIds,
                        aiSummary = "Select a restaurant to see detailed sentiment intelligence.",
                        onRestaurantClick = appViewModel::openRestaurantDetail,
                        onFavoriteToggle = appViewModel::toggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Text(text = label.take(1)) },
        label = { Text(text = label) }
    )
}
