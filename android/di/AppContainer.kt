package com.example.vibevision.di

import android.content.Context
import com.example.vibevision.BuildConfig
import com.example.vibevision.data.cache.MemoryCache
import com.example.vibevision.data.local.InMemoryLocalDatabase
import com.example.vibevision.data.local.SharedPreferencesUserProfileStorage
import com.example.vibevision.data.local.UserProfileStorage
import com.example.vibevision.data.remote.GooglePlacesRestaurantApiService
import com.example.vibevision.data.remote.LlmRecommendationService
import com.example.vibevision.data.remote.OpenAiLlmRecommendationService
import com.example.vibevision.data.repo.RestaurantRepository

object AppContainer {
    private var appContext: Context? = null
    // Dish lookup loaded from assets: "normalized_name|city" -> list of dish names
    private var dishLookup: Map<String, List<String>> = emptyMap()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        dishLookup = loadDishLookup(context)
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadDishLookup(context: Context): Map<String, List<String>> {
        return try {
            val json = context.assets.open("dish_mentions_by_business.json")
                .bufferedReader().use { it.readText() }
            val raw = org.json.JSONObject(json)
            buildMap {
                raw.keys().forEach { key ->
                    val arr = raw.getJSONArray(key)
                    put(key, List(arr.length()) { arr.getString(it) })
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private val localDatabase by lazy {
        InMemoryLocalDatabase(initialRestaurants = emptyList())
    }

    private val apiService by lazy {
        GooglePlacesRestaurantApiService.create(BuildConfig.GOOGLE_PLACES_WEB_API_KEY, dishLookup)
    }

    private val memoryCache by lazy {
        MemoryCache(ttlMs = 180_000L)
    }

    val llmRecommendationService: LlmRecommendationService? by lazy {
        if (BuildConfig.OPENAI_API_KEY.isBlank()) {
            null
        } else {
            OpenAiLlmRecommendationService.create(
                apiKey = BuildConfig.OPENAI_API_KEY,
                model = BuildConfig.OPENAI_MODEL
            )
        }
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
