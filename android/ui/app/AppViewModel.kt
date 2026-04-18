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
    val favoriteRestaurantIds: Set<String> = emptySet(),
    val recentlyViewedIds: List<String> = emptyList(),
    val showFilterOverlay: Boolean = false,
    val selectedCuisineFilters: Set<String> = emptySet(),
    val selectedPriceFilters: Set<Int> = emptySet(),
    val selectedVibeFilters: Set<String> = emptySet(),
    val isDarkMode: Boolean = false,
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
        val current = _uiState.value
        val updatedRecent = (listOf(restaurant.id) + current.recentlyViewedIds)
            .distinct()
            .take(8)

        _uiState.value = current.copy(
            selectedRestaurant = restaurant,
            route = AppRoute.DETAIL,
            recentlyViewedIds = updatedRecent
        )
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

    fun toggleFavorite(restaurantId: String) {
        val current = _uiState.value.favoriteRestaurantIds
        val updated = if (current.contains(restaurantId)) current - restaurantId else current + restaurantId
        _uiState.value = _uiState.value.copy(favoriteRestaurantIds = updated)
    }

    fun setFilterOverlayVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showFilterOverlay = visible)
    }

    fun toggleCuisineFilter(cuisine: String) {
        val current = _uiState.value.selectedCuisineFilters
        val updated = if (current.contains(cuisine)) current - cuisine else current + cuisine
        _uiState.value = _uiState.value.copy(selectedCuisineFilters = updated)
    }

    fun togglePriceFilter(priceLevel: Int) {
        val current = _uiState.value.selectedPriceFilters
        val updated = if (current.contains(priceLevel)) current - priceLevel else current + priceLevel
        _uiState.value = _uiState.value.copy(selectedPriceFilters = updated)
    }

    fun toggleVibeFilter(vibe: String) {
        val current = _uiState.value.selectedVibeFilters
        val updated = if (current.contains(vibe)) current - vibe else current + vibe
        _uiState.value = _uiState.value.copy(selectedVibeFilters = updated)
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCuisineFilters = emptySet(),
            selectedPriceFilters = emptySet(),
            selectedVibeFilters = emptySet()
        )
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun favoriteRestaurants(): List<Restaurant> {
        val ids = _uiState.value.favoriteRestaurantIds
        return _uiState.value.restaurants.filter { ids.contains(it.id) }
    }

    fun recentlyViewedRestaurants(): List<Restaurant> {
        val byId = _uiState.value.restaurants.associateBy { it.id }
        return _uiState.value.recentlyViewedIds.mapNotNull { byId[it] }
    }

    fun allCuisines(): List<String> = _uiState.value.restaurants.map { it.cuisine }.distinct().sorted()

    fun allVibes(): List<String> = _uiState.value.restaurants.flatMap { it.vibeTags }.distinct().sorted()

    fun allPriceLevels(): List<Int> = _uiState.value.restaurants.map { it.priceLevel }.distinct().sorted()

    fun filteredRestaurants(): List<Restaurant> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()

        return state.restaurants.filter { r ->
            val matchesQuery =
                query.isEmpty() ||
                    r.name.lowercase().contains(query) ||
                    r.cuisine.lowercase().contains(query) ||
                    r.vibeTags.any { it.lowercase().contains(query) }

            val matchesCuisine = state.selectedCuisineFilters.isEmpty() || state.selectedCuisineFilters.contains(r.cuisine)
            val matchesPrice = state.selectedPriceFilters.isEmpty() || state.selectedPriceFilters.contains(r.priceLevel)
            val matchesVibe =
                state.selectedVibeFilters.isEmpty() ||
                    r.vibeTags.any { vibe -> state.selectedVibeFilters.contains(vibe) }

            matchesQuery && matchesCuisine && matchesPrice && matchesVibe
        }
    }
}
