package com.example.vibevision.di

import com.example.vibevision.data.SampleRestaurantData
import com.example.vibevision.data.local.InMemoryLocalDatabase
import com.example.vibevision.data.remote.FakeRestaurantApiService
import com.example.vibevision.data.repo.RestaurantRepository

object AppContainer {
    private val localDatabase by lazy {
        InMemoryLocalDatabase(initialRestaurants = SampleRestaurantData.restaurants)
    }

    private val apiService by lazy {
        FakeRestaurantApiService()
    }

    val restaurantRepository: RestaurantRepository by lazy {
        RestaurantRepository(
            api = apiService,
            localDatabase = localDatabase
        )
    }
}
