package com.example.vibevision.ui.app

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.example.vibevision.ui.screens.AccountSettingsScreen
import com.example.vibevision.ui.screens.AdvancedAnalyticsDashboardScreen
import com.example.vibevision.ui.screens.BasicProfileScreen
import com.example.vibevision.ui.screens.DishSentimentScreen
import com.example.vibevision.ui.screens.HomeFeedScreen
import com.example.vibevision.ui.screens.RestaurantDetailScreen
import com.example.vibevision.ui.screens.RestaurantSearchScreen
import com.example.vibevision.ui.screens.VibeMatchSetupScreen
import com.example.vibevision.ui.theme.VibeVisionTheme
import kotlinx.coroutines.launch

private sealed class AppDestination(val route: String, val label: String) {
    data object Home : AppDestination("home", "Home")
    data object Search : AppDestination("search", "Search")
    data object Analyzer : AppDestination("analyzer", "Analyze")
    data object Insights : AppDestination("insights", "Insights")
    data object Profile : AppDestination("profile", "Profile")
    data object AccountSettings : AppDestination("account_settings", "Account Settings")
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
    AppDestination.AccountSettings -> AppRoute.PROFILE
    AppDestination.Detail -> AppRoute.DETAIL
    AppDestination.VibeSetup -> AppRoute.PROFILE
    AppDestination.DishSentiment -> AppRoute.DETAIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeVisionApp(
    analyzer: ReviewSentimentPredictor,
    startInVibeSetup: Boolean = false,
    onStartDestinationConsumed: () -> Unit = {},
    appViewModel: AppViewModel = viewModel()
) {
    val state by appViewModel.uiState.collectAsStateWithLifecycle()
    val sentimentViewModel = remember(analyzer) { SentimentViewModel(analyzer) }
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val startDestination = remember {
        if (startInVibeSetup) AppDestination.VibeSetup.route else state.route.toDestination().route
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val primaryDestinations = listOf(
        AppDestination.Home,
        AppDestination.Search,
        AppDestination.Analyzer,
        AppDestination.Insights,
        AppDestination.Profile
    )

    val drawerDestinations = listOf(
        AppDestination.Home,
        AppDestination.Search,
        AppDestination.Analyzer,
        AppDestination.Insights,
        AppDestination.Profile,
        AppDestination.AccountSettings
    )

    val routeLabels = mapOf(
        AppDestination.Home.route to AppDestination.Home.label,
        AppDestination.Search.route to AppDestination.Search.label,
        AppDestination.Analyzer.route to AppDestination.Analyzer.label,
        AppDestination.Insights.route to AppDestination.Insights.label,
        AppDestination.Profile.route to AppDestination.Profile.label,
        AppDestination.AccountSettings.route to AppDestination.AccountSettings.label,
        AppDestination.Detail.route to "Restaurant Detail",
        AppDestination.VibeSetup.route to "Vibe Setup",
        AppDestination.DishSentiment.route to "Dish Sentiment"
    )
    val displayFirstName = remember(state.userProfile.name, state.userProfile.email) {
        val trimmedName = state.userProfile.name.trim()
        if (trimmedName.isNotEmpty()) {
            trimmedName.split(Regex("\\s+"))[0]
        } else {
            val emailPrefix = state.userProfile.email.substringBefore("@").trim()
            if (emailPrefix.isNotEmpty()) emailPrefix else "Profile"
        }
    }
    val isPrimaryRoute = primaryDestinations.any { it.route == currentRoute }
    val showBackButton = !isPrimaryRoute

    androidx.compose.runtime.LaunchedEffect(startInVibeSetup) {
        if (startInVibeSetup) onStartDestinationConsumed()
    }

    VibeVisionTheme(darkTheme = state.isDarkMode) {
        fun navigateTo(destination: AppDestination) {
            navController.navigate(destination.route) {
                if (primaryDestinations.contains(destination)) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    restoreState = true
                }
                launchSingleTop = true
            }

            if (primaryDestinations.contains(destination)) {
                appViewModel.navigate(destination.toAppRoute())
            }

            scope.launch { drawerState.close() }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        text = "VibeVision Dining",
                        modifier = Modifier.padding(16.dp)
                    )
                    drawerDestinations.forEach { destination ->
                        NavigationDrawerItem(
                            label = { Text(destination.label) },
                            selected = currentRoute == destination.route,
                            onClick = { navigateTo(destination) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(routeLabels[currentRoute] ?: "VibeVision Dining") },
                        navigationIcon = {
                            if (showBackButton) {
                                IconButton(onClick = {
                                    val popped = navController.popBackStack()
                                    if (!popped) {
                                        val fallback = when (currentRoute) {
                                            AppDestination.Detail.route,
                                            AppDestination.DishSentiment.route -> AppDestination.Home.route
                                            else -> AppDestination.Profile.route
                                        }
                                        navController.navigate(fallback) {
                                            launchSingleTop = true
                                        }
                                    }
                                }) {
                                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Open menu")
                                }
                            }
                        },
                        actions = {
                            TextButton(onClick = { navigateTo(AppDestination.Profile) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = displayFirstName,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Open profile"
                                    )
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    if (isPrimaryRoute) {
                        AppBottomNavigationBar(
                            items = primaryDestinations.map { NavDestinationItem(route = it.route, label = it.label) },
                            selectedRoute = currentRoute,
                            onNavigate = { item ->
                                val destination = primaryDestinations.first { it.route == item.route }
                                navigateTo(destination)
                            }
                        )
                    }
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
                            aiSummary = appViewModel.homeFeedSummary(),
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
                            isLoading = state.isSearchLoading,
                            errorMessage = state.searchErrorMessage,
                            showFilterOverlay = state.showFilterOverlay,
                            onQueryChange = appViewModel::updateSearchQuery,
                            onRestaurantClick = { restaurant ->
                                appViewModel.openRestaurantDetail(restaurant)
                                navController.navigate(AppDestination.Detail.route)
                            },
                            onFavoriteToggle = appViewModel::toggleFavorite,
                            onSetCity = appViewModel::setCity,
                            onSearchNow = appViewModel::searchByCurrentFilters,
                            onSearchNearMe = appViewModel::searchNearby,
                            onClearError = appViewModel::clearSearchError,
                            onToggleOverlay = { appViewModel.setFilterOverlayVisible(!state.showFilterOverlay) },
                            onToggleCuisine = appViewModel::toggleCuisineFilter,
                            onTogglePrice = appViewModel::togglePriceFilter,
                            onToggleVibe = appViewModel::toggleVibeFilter,
                            onClearFilters = appViewModel::clearAllFilters,
                            onShowAllRestaurants = appViewModel::resetSearch
                        )
                    }

                    composable(AppDestination.Analyzer.route) {
                        SentimentAnalysisScreen(viewModel = sentimentViewModel)
                    }

                    composable(AppDestination.Insights.route) {
                        AdvancedAnalyticsDashboardScreen(
                            snapshot = appViewModel.analyticsSnapshot(),
                            recommendations = appViewModel.personalizedRecommendations(),
                            vibeLeaderboard = appViewModel.vibeLeaderboard(),
                            hiddenGems = appViewModel.hiddenGemsSpotlight(),
                            recentlyViewed = appViewModel.recentlyViewedRestaurants(),
                            onRestaurantClick = { appViewModel.openRestaurantDetail(it) }
                        )
                    }

                    composable(AppDestination.Profile.route) {
                        BasicProfileScreen(
                            preferences = state.vibePreferences,
                            profileName = state.userProfile.name,
                            profileEmail = state.userProfile.email,
                            favoriteCount = state.favoriteRestaurantIds.size,
                            recommendationCount = appViewModel.personalizedRecommendations().size,
                            onOpenVibeSetup = { navController.navigate(AppDestination.VibeSetup.route) }
                        )
                    }

                    composable(AppDestination.AccountSettings.route) {
                        AccountSettingsScreen(
                            isDarkMode = state.isDarkMode,
                            isOfflineMode = state.isOfflineMode,
                            pushNotificationsEnabled = state.pushNotificationsEnabled,
                            language = state.language,
                            profileName = state.userProfile.name,
                            profileAddress = state.userProfile.address,
                            profilePhone = state.userProfile.phone,
                            email = state.userProfile.email,
                            profileSavedMessage = state.profileSavedMessage,
                            accountActionMessage = state.accountActionMessage,
                            onDarkModeToggle = appViewModel::setDarkMode,
                            onOfflineModeToggle = appViewModel::setOfflineMode,
                            onPushNotificationsToggle = appViewModel::setPushNotifications,
                            onLanguageChange = appViewModel::setLanguage,
                            onNameChange = appViewModel::setProfileName,
                            onAddressChange = appViewModel::setProfileAddress,
                            onPhoneChange = appViewModel::setProfilePhone,
                            onEmailChange = appViewModel::setProfileEmail,
                            onSaveProfile = appViewModel::saveUserProfile,
                            onDismissSavedMessage = appViewModel::clearProfileSavedMessage,
                            onChangePassword = appViewModel::changePassword,
                            onSignOut = appViewModel::signOut,
                            onDismissMessage = appViewModel::dismissAccountActionMessage
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
}
