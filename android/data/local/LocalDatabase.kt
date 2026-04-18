package com.example.vibevision.data.local

import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review

interface LocalDatabase {
    fun getRestaurants(): List<Restaurant>
    fun saveRestaurants(restaurants: List<Restaurant>)
    fun getFavoriteIds(): Set<String>
    fun saveFavoriteIds(ids: Set<String>)
    fun getUserReviews(): Map<String, List<Review>>
    fun appendUserReview(restaurantId: String, review: Review)
}

class InMemoryLocalDatabase(initialRestaurants: List<Restaurant>) : LocalDatabase {
    private var restaurants: List<Restaurant> = initialRestaurants
    private var favoriteIds: Set<String> = emptySet()
    private val userReviews: MutableMap<String, MutableList<Review>> = mutableMapOf()

    override fun getRestaurants(): List<Restaurant> = restaurants

    override fun saveRestaurants(restaurants: List<Restaurant>) {
        this.restaurants = restaurants
    }

    override fun getFavoriteIds(): Set<String> = favoriteIds

    override fun saveFavoriteIds(ids: Set<String>) {
        favoriteIds = ids
    }

    override fun getUserReviews(): Map<String, List<Review>> = userReviews.mapValues { it.value.toList() }

    override fun appendUserReview(restaurantId: String, review: Review) {
        val bucket = userReviews.getOrPut(restaurantId) { mutableListOf() }
        bucket.add(review)
    }
}
