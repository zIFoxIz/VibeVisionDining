package com.example.vibevision.data.repo

import com.example.vibevision.data.local.LocalDatabase
import com.example.vibevision.data.remote.RestaurantApiService
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review

class RestaurantRepository(
    private val api: RestaurantApiService,
    private val localDatabase: LocalDatabase
) {
    fun loadRestaurants(forceRefresh: Boolean = false): List<Restaurant> {
        val cached = localDatabase.getRestaurants()
        if (cached.isNotEmpty() && !forceRefresh) {
            return cached
        }

        val remote = api.fetchRestaurants()
        localDatabase.saveRestaurants(remote)
        return remote
    }

    fun getFavoriteIds(): Set<String> = localDatabase.getFavoriteIds()

    fun saveFavoriteIds(ids: Set<String>) {
        localDatabase.saveFavoriteIds(ids)
    }

    fun scrapeReviewStatus(): String = api.scrapeReviewStatus()

    fun appendUserReview(restaurantId: String, review: Review) {
        localDatabase.appendUserReview(restaurantId, review)
    }

    fun getUserReviews(): Map<String, List<Review>> = localDatabase.getUserReviews()
}
