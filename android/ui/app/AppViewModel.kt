package com.example.vibevision.ui.app

import androidx.lifecycle.ViewModel
import com.example.vibevision.data.SampleRestaurantData
import com.example.vibevision.model.LanguageOption
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.example.vibevision.model.VibePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppRoute {
    HOME,
    SEARCH,
    ANALYZER,
    PROFILE,
    DETAIL,
    INSIGHTS
}

data class AnalyticsSnapshot(
    val totalRestaurants: Int,
    val totalReviews: Int,
    val avgRating: Float,
    val topCity: String,
    val topCuisine: String
)

data class AppState(
    val route: AppRoute = AppRoute.HOME,
    val restaurants: List<Restaurant> = SampleRestaurantData.restaurants,
    val selectedRestaurant: Restaurant? = null,
    val searchQuery: String = "",
    val selectedCity: String = "All",
    val favoriteRestaurantIds: Set<String> = emptySet(),
    val recentlyViewedIds: List<String> = emptyList(),
    val showFilterOverlay: Boolean = false,
    val selectedCuisineFilters: Set<String> = emptySet(),
    val selectedPriceFilters: Set<Int> = emptySet(),
    val selectedVibeFilters: Set<String> = emptySet(),
    val isDarkMode: Boolean = false,
    val isOfflineMode: Boolean = false,
    val pushNotificationsEnabled: Boolean = false,
    val language: LanguageOption = LanguageOption.ENGLISH,
    val selectedShareTemplate: String = "Quick",
    val lastScrapeStatus: String = "Never run",
    val userSubmittedReviews: Map<String, List<Review>> = emptyMap(),
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

    fun setCity(city: String) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
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

    fun setOfflineMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isOfflineMode = enabled)
    }

    fun setPushNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
    }

    fun setLanguage(language: LanguageOption) {
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun setShareTemplate(template: String) {
        _uiState.value = _uiState.value.copy(selectedShareTemplate = template)
    }

    fun simulateRealTimeReviewScrape() {
        val stamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(lastScrapeStatus = "Scrape simulated at $stamp")
    }

    fun submitUserReview(restaurantId: String, text: String, rating: Int, category: ReviewCategory) {
        if (text.isBlank()) return

        val state = _uiState.value
        val existing = state.userSubmittedReviews[restaurantId].orEmpty()
        val next = existing + Review(
            id = "usr_${System.currentTimeMillis()}",
            text = text,
            rating = rating.coerceIn(1, 5),
            category = category
        )
        _uiState.value = state.copy(userSubmittedReviews = state.userSubmittedReviews + (restaurantId to next))
    }

    fun reviewsForRestaurant(restaurant: Restaurant): List<Review> {
        val extras = _uiState.value.userSubmittedReviews[restaurant.id].orEmpty()
        return restaurant.reviews + extras
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

    fun allCities(): List<String> = listOf("All") + _uiState.value.restaurants.map { it.city }.distinct().sorted()

    fun allVibes(): List<String> = _uiState.value.restaurants.flatMap { it.vibeTags }.distinct().sorted()

    fun allPriceLevels(): List<Int> = _uiState.value.restaurants.map { it.priceLevel }.distinct().sorted()

    fun filteredRestaurants(): List<Restaurant> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()

        return state.restaurants.filter { r ->
            val matchesCity = state.selectedCity == "All" || state.selectedCity == r.city
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

            matchesCity && matchesQuery && matchesCuisine && matchesPrice && matchesVibe
        }
    }

    fun personalizedRecommendations(): List<Restaurant> {
        val state = _uiState.value
        val enabled = state.vibePreferences.filter { it.enabled }.map { it.vibe }.toSet()

        return filteredRestaurants()
            .sortedByDescending { restaurant ->
                val vibeScore = restaurant.vibeTags.count { enabled.contains(it) }
                val favoriteBoost = if (state.favoriteRestaurantIds.contains(restaurant.id)) 2 else 0
                vibeScore + favoriteBoost
            }
            .take(5)
    }

    fun aiGeneratedSummary(restaurant: Restaurant): String {
        val reviews = reviewsForRestaurant(restaurant)
        val avg = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
        val sentimentWord = when {
            avg >= 4.2 -> "strongly positive"
            avg >= 3.4 -> "mostly positive"
            avg >= 2.7 -> "mixed"
            else -> "critical"
        }
        return "AI summary: ${restaurant.name} in ${restaurant.city} shows $sentimentWord review signals, with top mentions around ${restaurant.dishSentiments.firstOrNull()?.dishName ?: "house dishes"}."
    }

    fun restaurantVibeTimeline(restaurant: Restaurant): List<String> {
        val reviews = reviewsForRestaurant(restaurant)
        val recentCount = reviews.size
        return listOf(
            "Q1: Launch sentiment baseline collected",
            "Q2: Vibe tags stabilized (${restaurant.vibeTags.joinToString()})",
            "Q3: Community momentum increased ($recentCount tracked reviews)",
            "Q4: Recommendation readiness in progress"
        )
    }

    fun analyticsSnapshot(): AnalyticsSnapshot {
        val restaurants = _uiState.value.restaurants
        val allReviews = restaurants.flatMap { reviewsForRestaurant(it) }
        val avg = if (allReviews.isEmpty()) 0f else allReviews.map { it.rating }.average().toFloat()

        val topCity = restaurants.groupingBy { it.city }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"
        val topCuisine = restaurants.groupingBy { it.cuisine }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"

        return AnalyticsSnapshot(
            totalRestaurants = restaurants.size,
            totalReviews = allReviews.size,
            avgRating = avg,
            topCity = topCity,
            topCuisine = topCuisine
        )
    }
}
