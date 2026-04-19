package com.example.vibevision.data.repo

import com.example.vibevision.data.cache.MemoryCache
import com.example.vibevision.data.local.LocalDatabase
import com.example.vibevision.data.remote.RestaurantApiService
import com.example.vibevision.domain.SentimentAggregation
import com.example.vibevision.domain.RestaurantSentimentAggregate
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review

class RestaurantRepository(
    private val api: RestaurantApiService,
    private val localDatabase: LocalDatabase,
    private val cache: MemoryCache
) {
    private val restaurantsCacheKey = "restaurants:list"
    private val scrapeStatusKey = "scrape:status"

    fun loadRestaurants(forceRefresh: Boolean = false): List<Restaurant> {
        if (!forceRefresh) {
            cache.get<List<Restaurant>>(restaurantsCacheKey)?.let { return it }
        }

        val cached = localDatabase.getRestaurants()
        if (cached.isNotEmpty() && !forceRefresh) {
            cache.put(restaurantsCacheKey, cached)
            return cached
        }

        val remote = api.fetchRestaurants()
        localDatabase.saveRestaurants(remote)
        cache.put(restaurantsCacheKey, remote)
        return remote
    }

    fun getFavoriteIds(): Set<String> = localDatabase.getFavoriteIds()

    fun saveFavoriteIds(ids: Set<String>) {
        localDatabase.saveFavoriteIds(ids)
    }

    fun scrapeReviewStatus(forceRefresh: Boolean = true): String {
        if (!forceRefresh) {
            cache.get<String>(scrapeStatusKey)?.let { return it }
        }
        val status = api.scrapeReviewStatus()
        cache.put(scrapeStatusKey, status)
        return status
    }

    fun appendUserReview(restaurantId: String, review: Review) {
        localDatabase.appendUserReview(restaurantId, review)
        cache.invalidate(restaurantsCacheKey)
    }

    fun getUserReviews(): Map<String, List<Review>> = localDatabase.getUserReviews()

    fun aggregateRestaurantSentiment(restaurant: Restaurant, reviews: List<Review>): RestaurantSentimentAggregate {
        return SentimentAggregation.aggregateRestaurant(restaurant, reviews)
    }
}
