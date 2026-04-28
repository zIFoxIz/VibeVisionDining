package com.example.vibevision.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibevision.data.remote.LlmRecommendationService
import com.example.vibevision.data.repo.RestaurantRepository
import com.example.vibevision.di.AppContainer
import com.example.vibevision.domain.HeatmapCalculator
import com.example.vibevision.domain.DishSentimentAggregate
import com.example.vibevision.domain.RestaurantSentimentAggregate
import com.example.vibevision.domain.SentimentAggregation
import com.example.vibevision.domain.VibeMatchEngine
import com.example.vibevision.model.LanguageOption
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.example.vibevision.model.UserProfile
import com.example.vibevision.model.VibePreference
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val restaurants: List<Restaurant> = emptyList(),
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
    val userProfile: UserProfile = UserProfile(),
    val profileSavedMessage: String? = null,
    val accountActionMessage: String? = null,
    val selectedShareTemplate: String = "Quick",
    val isSearchLoading: Boolean = false,
    val searchErrorMessage: String? = null,
    val llmRecommendationIds: List<String> = emptyList(),
    val userSubmittedReviews: Map<String, List<Review>> = emptyMap(),
    val vibePreferences: List<VibePreference> = defaultVibePreferences()
)

private fun defaultVibePreferences(): List<VibePreference> {
    return listOf(
        VibePreference("Popular", true),
        VibePreference("Hidden Gem", true),
        VibePreference("Date Night", true),
        VibePreference("Casual", true),
        VibePreference("Family", true),
        VibePreference("Modern", true)
    )
}

class AppViewModel(
    private val repository: RestaurantRepository = AppContainer.restaurantRepository,
    private val llmRecommendationService: LlmRecommendationService? = AppContainer.llmRecommendationService
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()
    private val auth = FirebaseAuth.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        loadProfileForUser(firebaseAuth.currentUser?.uid, firebaseAuth.currentUser?.email)
    }
    private var searchJob: Job? = null
    private var llmRecommendationJob: Job? = null
    private val knownCityChips = listOf(
        "All", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "Austin", "San Jose"
    )

    init {
        auth.addAuthStateListener(authStateListener)
        loadProfileForUser(auth.currentUser?.uid, auth.currentUser?.email)

        viewModelScope.launch {
            runCatching {
                repository.loadRestaurants(forceRefresh = true)
            }.onSuccess { restaurants ->
                _uiState.value = _uiState.value.copy(
                    restaurants = restaurants,
                    favoriteRestaurantIds = repository.getFavoriteIds(),
                    userSubmittedReviews = repository.getUserReviews(),
                    searchErrorMessage = null
                )
                syncVibeOptionsWithRestaurants(restaurants)
                refreshLlmRecommendations()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    favoriteRestaurantIds = repository.getFavoriteIds(),
                    userSubmittedReviews = repository.getUserReviews(),
                    searchErrorMessage = error.message ?: "Unable to load restaurants right now."
                )
            }
        }
    }

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
        refreshLlmRecommendations()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        triggerSearchDebounced()
    }

    fun setCity(city: String) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        triggerSearchDebounced()
    }

    fun searchByCurrentFilters() {
        viewModelScope.launch {
            performRemoteSearch()
        }
    }

    fun searchNearby(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            performRemoteSearch(latitude = latitude, longitude = longitude)
        }
    }

    fun clearSearchError() {
        _uiState.value = _uiState.value.copy(searchErrorMessage = null)
    }

    fun toggleVibe(vibe: String) {
        val updated = _uiState.value.vibePreferences.map {
            if (it.vibe == vibe) it.copy(enabled = !it.enabled) else it
        }
        _uiState.value = _uiState.value.copy(vibePreferences = updated)
        refreshLlmRecommendations()
    }

    fun toggleFavorite(restaurantId: String) {
        val current = _uiState.value.favoriteRestaurantIds
        val updated = if (current.contains(restaurantId)) current - restaurantId else current + restaurantId
        repository.saveFavoriteIds(updated)
        _uiState.value = _uiState.value.copy(favoriteRestaurantIds = updated)
        refreshLlmRecommendations()
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

    fun resetSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCity = "All",
            selectedCuisineFilters = emptySet(),
            selectedPriceFilters = emptySet(),
            selectedVibeFilters = emptySet(),
            showFilterOverlay = false,
            searchErrorMessage = null
        )
        searchByCurrentFilters()
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun setOfflineMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isOfflineMode = enabled,
            llmRecommendationIds = if (enabled) emptyList() else _uiState.value.llmRecommendationIds
        )
        if (!enabled) refreshLlmRecommendations()
    }

    fun setPushNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
    }

    fun setLanguage(language: LanguageOption) {
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun changePassword() {
        val fallbackEmail = _uiState.value.userProfile.email.ifBlank {
            FirebaseAuth.getInstance().currentUser?.email.orEmpty()
        }

        if (fallbackEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(
                accountActionMessage = "Enter your email in Profile first to receive a reset link."
            )
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(fallbackEmail)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    accountActionMessage = "Password reset email sent to $fallbackEmail."
                )
            }
            .addOnFailureListener { error ->
                _uiState.value = _uiState.value.copy(
                    accountActionMessage = error.message ?: "Unable to send reset email right now."
                )
            }
    }

    fun signOut() {
        runCatching { auth.signOut() }
        _uiState.value = _uiState.value.copy(
            accountActionMessage = "Signed out."
        )
    }

    fun dismissAccountActionMessage() {
        _uiState.value = _uiState.value.copy(accountActionMessage = null)
    }

    fun setProfileName(name: String) {
        _uiState.value = _uiState.value.copy(
            userProfile = _uiState.value.userProfile.copy(name = name),
            profileSavedMessage = null
        )
        refreshLlmRecommendations()
    }

    fun setProfileDob(dateOfBirth: String) {
        _uiState.value = _uiState.value.copy(
            userProfile = _uiState.value.userProfile.copy(dateOfBirth = dateOfBirth),
            profileSavedMessage = null
        )
    }

    fun setProfileAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            userProfile = _uiState.value.userProfile.copy(address = address),
            profileSavedMessage = null
        )
    }

    fun setProfilePhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            userProfile = _uiState.value.userProfile.copy(phone = phone),
            profileSavedMessage = null
        )
    }

    fun setProfileEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            userProfile = _uiState.value.userProfile.copy(email = email),
            profileSavedMessage = null
        )
        refreshLlmRecommendations()
    }

    fun saveUserProfile() {
        val profile = _uiState.value.userProfile
        runCatching {
            AppContainer.userProfileStorage.saveProfile(auth.currentUser?.uid, profile)
        }
        _uiState.value = _uiState.value.copy(profileSavedMessage = "Profile saved")
    }

    private fun loadProfileForUser(userId: String?, authEmail: String?) {
        val savedProfile = runCatching { AppContainer.userProfileStorage.loadProfile(userId) }
            .getOrDefault(UserProfile())

        val mergedEmail = savedProfile.email.ifBlank { authEmail.orEmpty() }
        _uiState.value = _uiState.value.copy(
            userProfile = savedProfile.copy(email = mergedEmail),
            profileSavedMessage = null
        )
    }

    override fun onCleared() {
        llmRecommendationJob?.cancel()
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }

    fun clearProfileSavedMessage() {
        _uiState.value = _uiState.value.copy(profileSavedMessage = null)
    }

    fun setShareTemplate(template: String) {
        _uiState.value = _uiState.value.copy(selectedShareTemplate = template)
    }

    fun submitUserReview(restaurantId: String, text: String, rating: Int, category: ReviewCategory) {
        if (text.isBlank()) return

        val state = _uiState.value
        val existing = state.userSubmittedReviews[restaurantId].orEmpty()
        val newReview = Review(
            id = "usr_${System.currentTimeMillis()}",
            text = text,
            rating = rating.coerceIn(1, 5),
            category = category
        )
        val next = existing + newReview
        repository.appendUserReview(restaurantId, newReview)
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

    fun allCities(): List<String> {
        val dynamic = _uiState.value.restaurants.map { it.city }.filter { it.isNotBlank() }.distinct().sorted()
        return (knownCityChips + dynamic).distinct()
    }

    fun allVibes(): List<String> {
        val state = _uiState.value
        return mergeDistinctVibes(
            preferred = state.vibePreferences.map { it.vibe },
            discovered = state.restaurants.flatMap { it.vibeTags }
        )
    }

    fun allPriceLevels(): List<Int> {
        return _uiState.value.restaurants
            .filter { it.hasLivePriceLevel }
            .map { it.priceLevel }
            .distinct()
            .sorted()
    }

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
            val matchesPrice =
                state.selectedPriceFilters.isEmpty() ||
                    (r.hasLivePriceLevel && state.selectedPriceFilters.contains(r.priceLevel))
            val matchesVibe =
                state.selectedVibeFilters.isEmpty() ||
                    r.vibeTags.any { vibe ->
                        state.selectedVibeFilters.any { selected -> selected.equals(vibe, ignoreCase = true) }
                    }

            matchesCity && matchesQuery && matchesCuisine && matchesPrice && matchesVibe
        }
    }

    fun personalizedRecommendations(): List<Restaurant> {
        val state = _uiState.value
        val byId = filteredRestaurants().associateBy { it.id }

        val llmRanked = state.llmRecommendationIds
            .mapNotNull { byId[it] }
            .take(5)

        if (llmRanked.isNotEmpty()) {
            return llmRanked
        }

        return heuristicRecommendations(state)
    }

    private fun heuristicRecommendations(state: AppState): List<Restaurant> {
        return filteredRestaurants()
            .sortedByDescending { restaurant ->
                val reviews = reviewsForRestaurant(restaurant)
                val vibeScore = VibeMatchEngine.score(restaurant.vibeTags, state.vibePreferences, reviews)
                val favoriteBoost = if (state.favoriteRestaurantIds.contains(restaurant.id)) 2 else 0
                vibeScore + favoriteBoost
            }
            .take(5)
    }

    fun vibeMatchScore(restaurant: Restaurant): Float {
        return VibeMatchEngine.score(
            vibeTags = restaurant.vibeTags,
            preferences = _uiState.value.vibePreferences,
            reviews = reviewsForRestaurant(restaurant)
        )
    }

    fun vibeMatchExplanation(restaurant: Restaurant): String {
        return VibeMatchEngine.explain(vibeMatchScore(restaurant))
    }

    fun heatmapForRestaurant(restaurant: Restaurant): Map<String, Float> {
        val score = vibeMatchScore(restaurant)
        return HeatmapCalculator.compute(reviewsForRestaurant(restaurant), score)
    }

    fun dishSentimentAggregation(restaurant: Restaurant): Map<String, DishSentimentAggregate> {
        return restaurant.dishSentiments.associate { dish ->
            dish.dishName to SentimentAggregation.aggregateDish(dish)
        }
    }

    fun restaurantSentimentAggregation(restaurant: Restaurant): RestaurantSentimentAggregate {
        val reviews = reviewsForRestaurant(restaurant)
        return repository.aggregateRestaurantSentiment(restaurant, reviews)
    }

    fun aiGeneratedSummary(restaurant: Restaurant): String {
        val reviews = reviewsForRestaurant(restaurant)
        val highlightDishes = restaurant.dishSentiments
            .sortedByDescending { dish ->
                val mentions = dish.positive + dish.neutral + dish.negative
                (dish.positive - dish.negative) * 2 + mentions
            }
            .map { it.dishName }
            .take(2)
            .ifEmpty { restaurant.menuPreview.take(2) }

        if (reviews.isEmpty()) {
            val highlightText = if (highlightDishes.isNotEmpty()) {
                highlightDishes.joinToString(" and ")
            } else {
                "its menu highlights"
            }
            return "AI summary: ${restaurant.name} in ${restaurant.city} has limited live review text so far. Early signals suggest interest in $highlightText."
        }

        val sentiment = restaurantSentimentAggregation(restaurant)
        val positivePct = String.format("%.0f", sentiment.positiveReviewRatio * 100)
        val negativePct = String.format("%.0f", sentiment.negativeReviewRatio * 100)

        val dominantCategory = reviews
            .groupingBy { it.category }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        val concernCategory = reviews
            .filter { it.rating <= 2 }
            .groupingBy { it.category }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        val categoryFocus = when (dominantCategory) {
            ReviewCategory.FOOD -> "food quality"
            ReviewCategory.SERVICE -> "service experience"
            ReviewCategory.ATMOSPHERE -> "atmosphere"
            ReviewCategory.VALUE -> "value for price"
            null -> "overall dining experience"
        }

        val concernText = when (concernCategory) {
            ReviewCategory.FOOD -> "Most low-score notes mention food consistency."
            ReviewCategory.SERVICE -> "Most low-score notes mention service speed or attentiveness."
            ReviewCategory.ATMOSPHERE -> "Most low-score notes mention atmosphere or noise."
            ReviewCategory.VALUE -> "Most low-score notes mention price-to-value expectations."
            null -> ""
        }

        val highlightsText = if (highlightDishes.isNotEmpty()) {
            highlightDishes.joinToString(" and ")
        } else {
            "popular menu items"
        }

        val sentimentTone = when {
            sentiment.averageRating >= 4.3f -> "strong"
            sentiment.averageRating >= 3.6f -> "mostly positive"
            sentiment.averageRating >= 2.8f -> "mixed"
            else -> "critical"
        }

        val baseSummary =
            "AI summary: ${restaurant.name} is showing $sentimentTone sentiment (${String.format("%.1f", sentiment.averageRating)}/5 avg, $positivePct% positive vs $negativePct% negative). Reviews focus most on $categoryFocus, with frequent mentions of $highlightsText."

        return if (concernText.isNotBlank()) "$baseSummary $concernText" else baseSummary
    }

    fun vibeLeaderboard(): List<Pair<Restaurant, Float>> {
        return _uiState.value.restaurants
            .map { it to vibeMatchScore(it) }
            .sortedByDescending { it.second }
            .take(3)
    }

    fun hiddenGemsSpotlight(): List<Restaurant> {
        val favIds = _uiState.value.favoriteRestaurantIds
        return _uiState.value.restaurants
            .filter { r ->
                r.vibeTags.any { it.equals("Hidden Gem", ignoreCase = true) } &&
                    !favIds.contains(r.id)
            }
            .sortedByDescending { r ->
                val reviews = reviewsForRestaurant(r)
                if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
            }
            .take(3)
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

    fun homeFeedSummary(): String {
        val restaurants = _uiState.value.restaurants
        if (restaurants.isEmpty()) {
            return "No live restaurant results loaded yet. Run a search to fetch places data."
        }

        val reviews = restaurants.flatMap { reviewsForRestaurant(it) }
        val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
        val topCuisine = restaurants.groupingBy { it.cuisine }.eachCount().maxByOrNull { it.value }?.key ?: "Unknown"
        val pricedCount = restaurants.count { it.hasLivePriceLevel }

        return if (reviews.isNotEmpty()) {
            "Live trend: ${String.format("%.1f", avgRating)}/5 avg rating across ${reviews.size} review(s). Top cuisine in current results: $topCuisine. Live price tiers available for $pricedCount of ${restaurants.size} restaurants."
        } else {
            "Live trend: ${restaurants.size} restaurants loaded. Top cuisine in current results: $topCuisine. Live price tiers available for $pricedCount of ${restaurants.size} restaurants."
        }
    }

    private fun triggerSearchDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(450)
            performRemoteSearch()
        }
    }

    private suspend fun performRemoteSearch(
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val snapshot = _uiState.value
        _uiState.value = snapshot.copy(isSearchLoading = true, searchErrorMessage = null)

        runCatching {
            repository.searchRestaurants(
                query = snapshot.searchQuery,
                city = snapshot.selectedCity,
                latitude = latitude,
                longitude = longitude
            )
        }.onSuccess { restaurants ->
            _uiState.value = _uiState.value.copy(
                restaurants = restaurants,
                isSearchLoading = false,
                searchErrorMessage = null
            )
            syncVibeOptionsWithRestaurants(restaurants)
            refreshLlmRecommendations()
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                isSearchLoading = false,
                searchErrorMessage = toFriendlySearchMessage(error)
            )
        }
    }

    private fun refreshLlmRecommendations() {
        val service = llmRecommendationService ?: return
        val state = _uiState.value
        if (state.isOfflineMode) return

        val candidates = filteredRestaurants().ifEmpty { state.restaurants }.take(40)
        if (candidates.isEmpty()) {
            _uiState.value = _uiState.value.copy(llmRecommendationIds = emptyList())
            return
        }

        llmRecommendationJob?.cancel()
        llmRecommendationJob = viewModelScope.launch {
            runCatching {
                service.recommendRestaurantIds(
                    restaurants = candidates,
                    profile = _uiState.value.userProfile,
                    vibePreferences = _uiState.value.vibePreferences,
                    favoriteIds = _uiState.value.favoriteRestaurantIds,
                    recentlyViewedIds = _uiState.value.recentlyViewedIds,
                    maxResults = 5
                )
            }.onSuccess { ids ->
                _uiState.value = _uiState.value.copy(llmRecommendationIds = ids)
            }
        }
    }

    private fun toFriendlySearchMessage(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("REQUEST_DENIED", ignoreCase = true) ->
                "Search is blocked by API key restrictions. Check PLACES_WEB_API_KEY settings in Google Cloud."
            message.contains("OVER_QUERY_LIMIT", ignoreCase = true) ->
                "Search quota reached. Try again later or increase your Places API quota."
            message.contains("PLACES_WEB_API_KEY is missing", ignoreCase = true) ->
                "Search key missing. Add PLACES_WEB_API_KEY in android/local.properties."
            message.contains("network", ignoreCase = true) || message.contains("timeout", ignoreCase = true) ->
                "Network issue while searching. Check connection and try again."
            message.isNotBlank() -> message
            else -> "Could not load restaurants right now."
        }
    }

    private fun syncVibeOptionsWithRestaurants(restaurants: List<Restaurant>) {
        val state = _uiState.value
        val discovered = restaurants.flatMap { it.vibeTags }
        val mergedVibes = mergeDistinctVibes(
            preferred = state.vibePreferences.map { it.vibe },
            discovered = discovered
        )

        val existingByKey = state.vibePreferences.associateBy { normalizeVibeKey(it.vibe) }
        val mergedPreferences = mergedVibes.map { vibe ->
            existingByKey[normalizeVibeKey(vibe)]?.copy(vibe = vibe) ?: VibePreference(vibe, false)
        }

        val availableKeys = mergedPreferences.map { normalizeVibeKey(it.vibe) }.toSet()
        val sanitizedSelectedFilters = state.selectedVibeFilters
            .filter { availableKeys.contains(normalizeVibeKey(it)) }
            .toSet()

        _uiState.value = state.copy(
            vibePreferences = mergedPreferences,
            selectedVibeFilters = sanitizedSelectedFilters
        )
    }

    private fun mergeDistinctVibes(preferred: List<String>, discovered: List<String>): List<String> {
        val result = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        (preferred + discovered)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { vibe ->
                val key = normalizeVibeKey(vibe)
                if (seen.add(key)) {
                    result.add(vibe)
                }
            }

        return result
    }

    private fun normalizeVibeKey(value: String): String = value.trim().lowercase()
}
