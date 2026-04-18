package com.example.vibevision.data.remote

import com.example.vibevision.data.SampleRestaurantData
import com.example.vibevision.model.Restaurant

interface RestaurantApiService {
    fun fetchRestaurants(): List<Restaurant>
    fun scrapeReviewStatus(): String
}

class FakeRestaurantApiService : RestaurantApiService {
    override fun fetchRestaurants(): List<Restaurant> = SampleRestaurantData.restaurants

    override fun scrapeReviewStatus(): String {
        val stamp = System.currentTimeMillis()
        return "Scrape simulated at $stamp"
    }
}
