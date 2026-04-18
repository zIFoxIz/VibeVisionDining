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
import com.example.vibevision.ui.screens.AdvancedAnalyticsDashboardScreen
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
                    NavItem("Insights", state.route == AppRoute.INSIGHTS) { appViewModel.navigate(AppRoute.INSIGHTS) }
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
                        recommendations = appViewModel.personalizedRecommendations(),
                        isOfflineMode = state.isOfflineMode,
                        aiSummary = "Overall trend is positive, with strongest sentiment around food quality and service.",
                        onRestaurantClick = appViewModel::openRestaurantDetail,
                        onFavoriteToggle = appViewModel::toggleFavorite
                    )

                    AppRoute.SEARCH -> RestaurantSearchScreen(
                        query = state.searchQuery,
                        restaurants = appViewModel.filteredRestaurants(),
                        selectedCity = state.selectedCity,
                        availableCities = appViewModel.allCities(),
                        availableCuisines = appViewModel.allCuisines(),
                        availablePrices = appViewModel.allPriceLevels(),
                        availableVibes = appViewModel.allVibes(),
                        selectedCuisines = state.selectedCuisineFilters,
                        selectedPrices = state.selectedPriceFilters,
                        selectedVibes = state.selectedVibeFilters,
                        showFilterOverlay = state.showFilterOverlay,
                        onQueryChange = appViewModel::updateSearchQuery,
                        onRestaurantClick = appViewModel::openRestaurantDetail,
                        onSetCity = appViewModel::setCity,
                        onToggleOverlay = { appViewModel.setFilterOverlayVisible(!state.showFilterOverlay) },
                        onToggleCuisine = appViewModel::toggleCuisineFilter,
                        onTogglePrice = appViewModel::togglePriceFilter,
                        onToggleVibe = appViewModel::toggleVibeFilter,
                        onClearFilters = appViewModel::clearAllFilters
                    )

                    AppRoute.ANALYZER -> SentimentAnalysisScreen(viewModel = sentimentViewModel)

                    AppRoute.INSIGHTS -> AdvancedAnalyticsDashboardScreen(
                        snapshot = appViewModel.analyticsSnapshot(),
                        recommendations = appViewModel.personalizedRecommendations(),
                        scrapeStatus = state.lastScrapeStatus,
                        onSimulateScrape = appViewModel::simulateRealTimeReviewScrape
                    )

                    AppRoute.PROFILE -> BasicProfileScreen(
                        preferences = state.vibePreferences,
                        isDarkMode = state.isDarkMode,
                        isOfflineMode = state.isOfflineMode,
                        pushNotificationsEnabled = state.pushNotificationsEnabled,
                        language = state.language,
                        onToggle = appViewModel::toggleVibe,
                        onDarkModeToggle = appViewModel::setDarkMode,
                        onOfflineModeToggle = appViewModel::setOfflineMode,
                        onPushNotificationsToggle = appViewModel::setPushNotifications,
                        onLanguageChange = appViewModel::setLanguage
                    )

                    AppRoute.DETAIL -> state.selectedRestaurant?.let { restaurant ->
                        RestaurantDetailScreen(
                            restaurant = restaurant,
                            reviews = appViewModel.reviewsForRestaurant(restaurant),
                            vibePreferences = state.vibePreferences,
                            isFavorite = state.favoriteRestaurantIds.contains(restaurant.id),
                            aiSummary = appViewModel.aiGeneratedSummary(restaurant),
                            timeline = appViewModel.restaurantVibeTimeline(restaurant),
                            selectedShareTemplate = state.selectedShareTemplate,
                            onFavoriteToggle = { appViewModel.toggleFavorite(restaurant.id) },
                            onShareRestaurantCard = {
                                val text = when (state.selectedShareTemplate) {
                                    "Family Plan" -> "Family pick: ${restaurant.name} in ${restaurant.city}. Comfort vibe and menu picks include ${restaurant.menuPreview.take(2).joinToString()}."
                                    "Date Night" -> "Date night option: ${restaurant.name}. Mood: ${restaurant.vibeTags.joinToString()}"
                                    "Foodie" -> "Foodie alert: ${restaurant.name} top dishes ${restaurant.menuPreview.take(3).joinToString()}"
                                    else -> "${restaurant.name} • ${restaurant.city} • ${restaurant.cuisine} • vibe: ${restaurant.vibeTags.joinToString()}"
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share restaurant"))
                            },
                            onShareTemplateChange = appViewModel::setShareTemplate,
                            onSubmitReview = { text, rating, category ->
                                appViewModel.submitUserReview(restaurant.id, text, rating, category)
                            }
                        )
                    } ?: HomeFeedScreen(
                        restaurants = state.restaurants,
                        favorites = appViewModel.favoriteRestaurants(),
                        recentlyViewed = appViewModel.recentlyViewedRestaurants(),
                        favoriteIds = state.favoriteRestaurantIds,
                        recommendations = appViewModel.personalizedRecommendations(),
                        isOfflineMode = state.isOfflineMode,
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
