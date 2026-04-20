package com.example.vibevision.di

import android.content.Context
import com.example.vibevision.BuildConfig
import com.example.vibevision.data.SampleRestaurantData
import com.example.vibevision.data.cache.MemoryCache
import com.example.vibevision.data.local.InMemoryLocalDatabase
import com.example.vibevision.data.local.SharedPreferencesUserProfileStorage
import com.example.vibevision.data.local.UserProfileStorage
import com.example.vibevision.data.remote.GooglePlacesRestaurantApiService
import com.example.vibevision.data.repo.RestaurantRepository

object AppContainer {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val localDatabase by lazy {
        InMemoryLocalDatabase(initialRestaurants = SampleRestaurantData.restaurants)
    }

    private val apiService by lazy {
        GooglePlacesRestaurantApiService.create(BuildConfig.GOOGLE_PLACES_WEB_API_KEY)
    }

    private val memoryCache by lazy {
        MemoryCache(ttlMs = 180_000L)
    }

    val userProfileStorage: UserProfileStorage by lazy {
        val context = requireNotNull(appContext) { "AppContainer.initialize must be called before use." }
        SharedPreferencesUserProfileStorage(context)
    }

    val restaurantRepository: RestaurantRepository by lazy {
        RestaurantRepository(
            api = apiService,
            localDatabase = localDatabase,
            cache = memoryCache
        )
    }
}
