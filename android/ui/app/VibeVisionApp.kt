package com.example.vibevision.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
                    aiSummary = "Overall trend is positive, with strongest sentiment around food quality and service.",
                    onRestaurantClick = appViewModel::openRestaurantDetail
                )

                AppRoute.SEARCH -> RestaurantSearchScreen(
                    query = state.searchQuery,
                    restaurants = appViewModel.filteredRestaurants(),
                    onQueryChange = appViewModel::updateSearchQuery,
                    onRestaurantClick = appViewModel::openRestaurantDetail
                )

                AppRoute.ANALYZER -> SentimentAnalysisScreen(viewModel = sentimentViewModel)

                AppRoute.PROFILE -> BasicProfileScreen(
                    preferences = state.vibePreferences,
                    onToggle = appViewModel::toggleVibe
                )

                AppRoute.DETAIL -> state.selectedRestaurant?.let {
                    RestaurantDetailScreen(restaurant = it, vibePreferences = state.vibePreferences)
                } ?: HomeFeedScreen(
                    restaurants = state.restaurants,
                    aiSummary = "Select a restaurant to see detailed sentiment intelligence.",
                    onRestaurantClick = appViewModel::openRestaurantDetail
                )
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
