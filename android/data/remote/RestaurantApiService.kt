package com.example.vibevision.data.remote

import com.example.vibevision.model.DishSentiment
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RestaurantApiService {
    suspend fun fetchRestaurants(): List<Restaurant>
    suspend fun searchRestaurants(
        query: String,
        city: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): List<Restaurant>
}

data class PlacesSearchResponse(
    val results: List<PlacesSearchResult> = emptyList(),
    val status: String = ""
)

data class PlacesSearchResult(
    val place_id: String? = null,
    val name: String? = null,
    val formatted_address: String? = null,
    val vicinity: String? = null,
    val types: List<String>? = null,
    val price_level: Int? = null,
    val rating: Double? = null,
    val geometry: PlacesGeometry? = null
)

data class PlacesGeometry(
    val location: PlacesLocation? = null
)

data class PlacesLocation(
    val lat: Double? = null,
    val lng: Double? = null
)

data class ApiReview(
    val id: String,
    val text: String,
    val rating: Int,
    val category: String
)

data class ApiDishSentiment(
    val dishName: String,
    val positive: Int,
    val neutral: Int,
    val negative: Int
)

data class ApiRestaurant(
    val id: String,
    val name: String,
    val city: String,
    val cuisine: String,
    val priceLevel: Int,
    val vibeTags: List<String>,
    val photoLabels: List<String>,
    val menuPreview: List<String>,
    val reviews: List<ApiReview>,
    val dishSentiments: List<ApiDishSentiment>
)

private interface RestaurantBackendApi {
    @GET("restaurants")
    suspend fun fetchRestaurants(): List<ApiRestaurant>
}

private interface GooglePlacesApi {
    @GET("maps/api/place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,
        @Query("type") type: String,
        @Query("region") region: String,
        @Query("key") apiKey: String
    ): PlacesSearchResponse

    @GET("maps/api/place/nearbysearch/json")
    suspend fun nearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("keyword") keyword: String?,
        @Query("key") apiKey: String
    ): PlacesSearchResponse
}

class RealRestaurantApiService private constructor(
    private val backendApi: RestaurantBackendApi
) : RestaurantApiService {
    override suspend fun fetchRestaurants(): List<Restaurant> {
        return backendApi.fetchRestaurants().map { restaurant ->
            Restaurant(
                id = restaurant.id,
                name = restaurant.name,
                city = restaurant.city,
                cuisine = restaurant.cuisine,
                priceLevel = restaurant.priceLevel,
                vibeTags = restaurant.vibeTags,
                photoLabels = restaurant.photoLabels,
                menuPreview = restaurant.menuPreview,
                reviews = restaurant.reviews.map { review ->
                    Review(
                        id = review.id,
                        text = review.text,
                        rating = review.rating,
                        category = runCatching { ReviewCategory.valueOf(review.category.uppercase()) }
                            .getOrElse { ReviewCategory.FOOD }
                    )
                },
                dishSentiments = restaurant.dishSentiments.map { dish ->
                    DishSentiment(
                        dishName = dish.dishName,
                        positive = dish.positive,
                        neutral = dish.neutral,
                        negative = dish.negative
                    )
                }
            )
        }
    }

    override suspend fun searchRestaurants(
        query: String,
        city: String,
        latitude: Double?,
        longitude: Double?
    ): List<Restaurant> {
        val restaurants = fetchRestaurants()
        val queryText = query.trim().lowercase()
        val cityText = city.trim().lowercase()

        return restaurants.filter { restaurant ->
            val queryMatch =
                queryText.isBlank() ||
                    restaurant.name.lowercase().contains(queryText) ||
                    restaurant.cuisine.lowercase().contains(queryText)
            val cityMatch = cityText.isBlank() || cityText == "all" || restaurant.city.lowercase().contains(cityText)
            queryMatch && cityMatch
        }
    }

    companion object {
        fun create(baseUrl: String): RealRestaurantApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return RealRestaurantApiService(
                backendApi = retrofit.create(RestaurantBackendApi::class.java)
            )
        }
    }
}

class GooglePlacesRestaurantApiService private constructor(
    private val placesApi: GooglePlacesApi,
    private val apiKey: String
) : RestaurantApiService {
    override suspend fun fetchRestaurants(): List<Restaurant> {
        return searchRestaurants(query = "", city = "All")
    }

    override suspend fun searchRestaurants(
        query: String,
        city: String,
        latitude: Double?,
        longitude: Double?
    ): List<Restaurant> {
        if (apiKey.isBlank()) {
            throw IllegalStateException("PLACES_WEB_API_KEY is missing. Add it to android/local.properties.")
        }

        return try {
            val response = if (latitude != null && longitude != null) {
                placesApi.nearbySearch(
                    location = "$latitude,$longitude",
                    radius = 5000,
                    type = "restaurant",
                    keyword = query.trim().ifBlank { null },
                    apiKey = apiKey
                )
            } else {
                val citySegment = if (city.equals("All", ignoreCase = true) || city.isBlank()) "" else " in $city"
                val searchText = if (query.isBlank()) {
                    "restaurants$citySegment"
                } else {
                    "$query restaurant$citySegment"
                }

                placesApi.textSearch(
                    query = searchText,
                    type = "restaurant",
                    region = "us",
                    apiKey = apiKey
                )
            }

            if (!response.status.equals("OK", ignoreCase = true) && !response.status.equals("ZERO_RESULTS", ignoreCase = true)) {
                throw IllegalStateException(
                    "Google Places search failed: ${response.status}. " +
                        "Use a web-service key in PLACES_WEB_API_KEY and enable Places API."
                )
            }

            val mapped = response.results.map { it.toRestaurant() }
            val deduped = linkedMapOf<String, Restaurant>()
            mapped.forEach { deduped.putIfAbsent(it.id, it) }

            val filteredByCity = if (city.equals("All", ignoreCase = true) || city.isBlank()) {
                deduped.values.toList()
            } else {
                deduped.values.filter { it.city.contains(city, ignoreCase = true) }
            }

            filteredByCity
        } catch (error: Exception) {
            throw IllegalStateException(error.message ?: "Google Places request failed.", error)
        }
    }

    private fun PlacesSearchResult.toRestaurant(): Restaurant {
        val addressText = formatted_address ?: vicinity
        val cityName = extractCity(addressText)
        val cuisineGuess = inferCuisine(types.orEmpty())
        val price = (price_level ?: 2).coerceIn(1, 4)
        val ratingValue = (rating ?: 4.0).coerceIn(1.0, 5.0)
        val safeName = name?.takeIf { it.isNotBlank() } ?: "Unnamed Restaurant"
        val safePlaceId = place_id?.takeIf { it.isNotBlank() }
            ?: "gm_${safeName.lowercase().replace(" ", "_")}_${cityName.lowercase().replace(" ", "_")}"
        val vibeTags = buildList {
            add(if (ratingValue >= 4.3) "Popular" else "Hidden Gem")
            add(if (price >= 3) "Date Night" else "Casual")
            add(if (price <= 2) "Family" else "Modern")
        }.distinct()

        return Restaurant(
            id = safePlaceId,
            name = safeName,
            city = cityName,
            cuisine = cuisineGuess,
            priceLevel = price,
            vibeTags = vibeTags,
            photoLabels = emptyList(),
            menuPreview = emptyList(),
            reviews = emptyList(),
            dishSentiments = emptyList()
        )
    }

    private fun extractCity(address: String?): String {
        val parts = address.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            parts.size >= 4 -> parts[parts.size - 3]
            parts.size >= 2 -> parts[parts.size - 2]
            else -> "Unknown"
        }
    }

    private fun inferCuisine(types: List<String>): String {
        val excluded = setOf(
            "restaurant", "food", "point_of_interest", "establishment", "store", "meal_takeaway", "meal_delivery"
        )
        val candidate = types.firstOrNull { !excluded.contains(it) }
            ?.replace('_', ' ')
            ?.split(" ")
            ?.joinToString(" ") { token -> token.replaceFirstChar { it.uppercase() } }
        return candidate ?: "Restaurant"
    }

    companion object {
        fun create(apiKey: String): GooglePlacesRestaurantApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return GooglePlacesRestaurantApiService(
                placesApi = retrofit.create(GooglePlacesApi::class.java),
                apiKey = apiKey
            )
        }
    }
}
