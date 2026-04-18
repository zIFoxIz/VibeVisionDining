package com.example.vibevision.ui.app

import androidx.lifecycle.ViewModel
import com.example.vibevision.data.SampleRestaurantData
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.VibePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppRoute {
    HOME,
    SEARCH,
    ANALYZER,
    PROFILE,
    DETAIL
}

data class AppState(
    val route: AppRoute = AppRoute.HOME,
    val restaurants: List<Restaurant> = SampleRestaurantData.restaurants,
    val selectedRestaurant: Restaurant? = null,
    val searchQuery: String = "",
    val vibePreferences: List<VibePreference> = listOf(
        VibePreference("Cozy", true),
        VibePreference("Energetic", true),
        VibePreference("Family", true),
        VibePreference("Romantic", false),
        VibePreference("Modern", true)
    )
)

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    fun navigate(route: AppRoute) {
        _uiState.value = _uiState.value.copy(route = route)
    }

    fun openRestaurantDetail(restaurant: Restaurant) {
        _uiState.value = _uiState.value.copy(selectedRestaurant = restaurant, route = AppRoute.DETAIL)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleVibe(vibe: String) {
        val updated = _uiState.value.vibePreferences.map {
            if (it.vibe == vibe) it.copy(enabled = !it.enabled) else it
        }
        _uiState.value = _uiState.value.copy(vibePreferences = updated)
    }

    fun filteredRestaurants(): List<Restaurant> {
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) return _uiState.value.restaurants

        return _uiState.value.restaurants.filter { r ->
            r.name.lowercase().contains(query) ||
                r.cuisine.lowercase().contains(query) ||
                r.vibeTags.any { it.lowercase().contains(query) }
        }
    }
}
