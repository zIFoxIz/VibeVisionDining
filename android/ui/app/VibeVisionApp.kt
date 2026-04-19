package com.example.vibevision.ui.app

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vibevision.ml.ReviewSentimentPredictor
import com.example.vibevision.ui.SentimentAnalysisScreen
import com.example.vibevision.ui.SentimentViewModel
import com.example.vibevision.ui.components.AppBottomNavigationBar
import com.example.vibevision.ui.components.NavDestinationItem
import com.example.vibevision.ui.screens.AdvancedAnalyticsDashboardScreen
import com.example.vibevision.ui.screens.BasicProfileScreen
import com.example.vibevision.ui.screens.DishSentimentScreen
import com.example.vibevision.ui.screens.HomeFeedScreen
import com.example.vibevision.ui.screens.RestaurantDetailScreen
import com.example.vibevision.ui.screens.RestaurantSearchScreen
import com.example.vibevision.ui.screens.VibeMatchSetupScreen
import com.example.vibevision.ui.theme.VibeVisionTheme

private sealed class AppDestination(val route: String, val label: String) {
    data object Home : AppDestination("home", "Home")
    data object Search : AppDestination("search", "Search")
    data object Analyzer : AppDestination("analyzer", "Analyze")
    data object Insights : AppDestination("insights", "Insights")
    data object Profile : AppDestination("profile", "Profile")
    data object Detail : AppDestination("detail", "Detail")
    data object VibeSetup : AppDestination("vibe_setup", "Vibes")
    data object DishSentiment : AppDestination("dish_sentiment", "Dish")
}

private fun AppRoute.toDestination(): AppDestination = when (this) {
    AppRoute.HOME -> AppDestination.Home
    AppRoute.SEARCH -> AppDestination.Search
    AppRoute.ANALYZER -> AppDestination.Analyzer
    AppRoute.INSIGHTS -> AppDestination.Insights
    AppRoute.PROFILE -> AppDestination.Profile
    AppRoute.DETAIL -> AppDestination.Detail
}

private fun AppDestination.toAppRoute(): AppRoute = when (this) {
    AppDestination.Home -> AppRoute.HOME
    AppDestination.Search -> AppRoute.SEARCH
    AppDestination.Analyzer -> AppRoute.ANALYZER
    AppDestination.Insights -> AppRoute.INSIGHTS
    AppDestination.Profile -> AppRoute.PROFILE
    AppDestination.Detail -> AppRoute.DETAIL
    AppDestination.VibeSetup -> AppRoute.PROFILE
    AppDestination.DishSentiment -> AppRoute.DETAIL
}

@Composable
fun VibeVisionApp(
    analyzer: ReviewSentimentPredictor,
    appViewModel: AppViewModel = viewModel()
) {
    val state by appViewModel.uiState.collectAsStateWithLifecycle()
    val sentimentViewModel = remember(analyzer) { SentimentViewModel(analyzer) }
    val context = LocalContext.current
    val navController = rememberNavController()
    val startDestination = remember { state.route.toDestination().route }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val primaryDestinations = listOf(
        AppDestination.Home,
        AppDestination.Search,
        AppDestination.Analyzer,
        AppDestination.Insights,
        AppDestination.Profile
    )

    VibeVisionTheme(darkTheme = state.isDarkMode) {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    items = primaryDestinations.map { NavDestinationItem(route = it.route, label = it.label) },
                    selectedRoute = currentRoute,
                    onNavigate = { item ->
                        val destination = primaryDestinations.first { it.route == item.route }
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        appViewModel.navigate(destination.toAppRoute())
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable(AppDestination.Home.route) {
                        HomeFeedScreen(
                            restaurants = state.restaurants,
                            favorites = appViewModel.favoriteRestaurants(),
                            recentlyViewed = appViewModel.recentlyViewedRestaurants(),
                            favoriteIds = state.favoriteRestaurantIds,
                            recommendations = appViewModel.personalizedRecommendations(),
                            isOfflineMode = state.isOfflineMode,
                            aiSummary = "Overall trend is positive, with strongest sentiment around food quality and service.",
                            onRestaurantClick = { restaurant ->
                                appViewModel.openRestaurantDetail(restaurant)
                                navController.navigate(AppDestination.Detail.route)
                            },
                            onFavoriteToggle = appViewModel::toggleFavorite
                        )
                    }

                    composable(AppDestination.Search.route) {
                        RestaurantSearchScreen(
                            query = state.searchQuery,
                            restaurants = appViewModel.filteredRestaurants(),
                            selectedCity = state.selectedCity,
                            availableCities = appViewModel.allCities(),
                            availableCuisines = appViewModel.allCuisines(),
                            availablePrices = appViewModel.allPriceLevels(),
                            availableVibes = appViewModel.allVibes(),
                            favoriteIds = state.favoriteRestaurantIds,
                            selectedCuisines = state.selectedCuisineFilters,
                            selectedPrices = state.selectedPriceFilters,
                            selectedVibes = state.selectedVibeFilters,
                            showFilterOverlay = state.showFilterOverlay,
                            onQueryChange = appViewModel::updateSearchQuery,
                            onRestaurantClick = { restaurant ->
                                appViewModel.openRestaurantDetail(restaurant)
                                navController.navigate(AppDestination.Detail.route)
                            },
                            onFavoriteToggle = appViewModel::toggleFavorite,
                            onSetCity = appViewModel::setCity,
                            onToggleOverlay = { appViewModel.setFilterOverlayVisible(!state.showFilterOverlay) },
                            onToggleCuisine = appViewModel::toggleCuisineFilter,
                            onTogglePrice = appViewModel::togglePriceFilter,
                            onToggleVibe = appViewModel::toggleVibeFilter,
                            onClearFilters = appViewModel::clearAllFilters
                        )
                    }

                    composable(AppDestination.Analyzer.route) {
                        SentimentAnalysisScreen(viewModel = sentimentViewModel)
                    }

                    composable(AppDestination.Insights.route) {
                        AdvancedAnalyticsDashboardScreen(
                            snapshot = appViewModel.analyticsSnapshot(),
                            recommendations = appViewModel.personalizedRecommendations(),
                            scrapeStatus = state.lastScrapeStatus,
                            onSimulateScrape = appViewModel::simulateRealTimeReviewScrape
                        )
                    }

                    composable(AppDestination.Profile.route) {
                        BasicProfileScreen(
                            preferences = state.vibePreferences,
                            isDarkMode = state.isDarkMode,
                            isOfflineMode = state.isOfflineMode,
                            pushNotificationsEnabled = state.pushNotificationsEnabled,
                            language = state.language,
                            onToggle = appViewModel::toggleVibe,
                            onDarkModeToggle = appViewModel::setDarkMode,
                            onOfflineModeToggle = appViewModel::setOfflineMode,
                            onPushNotificationsToggle = appViewModel::setPushNotifications,
                            onLanguageChange = appViewModel::setLanguage,
                            onOpenVibeSetup = { navController.navigate(AppDestination.VibeSetup.route) }
                        )
                    }

                    composable(AppDestination.VibeSetup.route) {
                        VibeMatchSetupScreen(
                            preferences = state.vibePreferences,
                            onToggle = appViewModel::toggleVibe
                        )
                    }

                    composable(AppDestination.Detail.route) {
                        state.selectedRestaurant?.let { restaurant ->
                            RestaurantDetailScreen(
                                restaurant = restaurant,
                                reviews = appViewModel.reviewsForRestaurant(restaurant),
                                vibePreferences = state.vibePreferences,
                                isFavorite = state.favoriteRestaurantIds.contains(restaurant.id),
                                vibeMatchScore = appViewModel.vibeMatchScore(restaurant),
                                vibeMatchDescription = appViewModel.vibeMatchExplanation(restaurant),
                                heatmapScores = appViewModel.heatmapForRestaurant(restaurant),
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
                                onOpenDishSentiment = {
                                    navController.navigate(AppDestination.DishSentiment.route)
                                },
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
                            onRestaurantClick = { restaurant ->
                                appViewModel.openRestaurantDetail(restaurant)
                                navController.navigate(AppDestination.Detail.route)
                            },
                            onFavoriteToggle = appViewModel::toggleFavorite
                        )
                    }

                    composable(AppDestination.DishSentiment.route) {
                        state.selectedRestaurant?.let { restaurant ->
                            DishSentimentScreen(restaurant = restaurant)
                        } ?: HomeFeedScreen(
                            restaurants = state.restaurants,
                            favorites = appViewModel.favoriteRestaurants(),
                            recentlyViewed = appViewModel.recentlyViewedRestaurants(),
                            favoriteIds = state.favoriteRestaurantIds,
                            recommendations = appViewModel.personalizedRecommendations(),
                            isOfflineMode = state.isOfflineMode,
                            aiSummary = "Select a restaurant to view dish sentiment details.",
                            onRestaurantClick = { selected ->
                                appViewModel.openRestaurantDetail(selected)
                                navController.navigate(AppDestination.Detail.route)
                            },
                            onFavoriteToggle = appViewModel::toggleFavorite
                        )
                    }
                }
            }
        }
    }
}
