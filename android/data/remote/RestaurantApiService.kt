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
import kotlin.math.roundToInt

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

data class PlaceDetailsResponse(
    val result: PlaceDetailsResult? = null,
    val status: String = ""
)

data class PlaceDetailsResult(
    val price_level: Int? = null,
    val reviews: List<PlaceReview>? = null
)

data class PlaceReview(
    val text: String? = null,
    val rating: Double? = null
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
    val hasLivePriceLevel: Boolean = true,
    val isAvgPriceEstimated: Boolean = false,
    val vibeTags: List<String>,
    val photoLabels: List<String>,
    val menuPreview: List<String>,
    val reviews: List<ApiReview>,
    val dishSentiments: List<ApiDishSentiment>,
    val avgPricePerPersonUsd: Double? = null
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

    @GET("maps/api/place/details/json")
    suspend fun placeDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String,
        @Query("key") apiKey: String
    ): PlaceDetailsResponse
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
                hasLivePriceLevel = restaurant.hasLivePriceLevel,
                isAvgPriceEstimated = restaurant.isAvgPriceEstimated,
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
                },
                avgPricePerPersonUsd = restaurant.avgPricePerPersonUsd
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
    private val apiKey: String,
    private val dishLookup: Map<String, List<String>> = emptyMap()
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

            val mapped = response.results.map { result ->
                val liveDetails = resolveLiveDetails(result.place_id)
                val livePriceLevel = result.price_level?.coerceIn(1, 4)
                    ?: liveDetails?.priceLevel
                result.toRestaurant(
                    resolvedPriceLevel = livePriceLevel,
                    liveReviews = liveDetails?.reviews.orEmpty()
                )
            }
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

    private data class LivePlaceDetails(
        val priceLevel: Int?,
        val reviews: List<Review>
    )

    private suspend fun resolveLiveDetails(placeId: String?): LivePlaceDetails? {
        val safeId = placeId?.trim().orEmpty()
        if (safeId.isBlank()) return null

        return runCatching {
            val details = placesApi.placeDetails(
                placeId = safeId,
                fields = "price_level,reviews",
                apiKey = apiKey
            )

            if (!details.status.equals("OK", ignoreCase = true)) {
                null
            } else {
                val priceLevel = details.result?.price_level?.coerceIn(1, 4)
                val mappedReviews = details.result?.reviews
                    .orEmpty()
                    .mapIndexedNotNull { index, review ->
                        val text = review.text?.trim().orEmpty()
                        if (text.isBlank()) return@mapIndexedNotNull null

                        Review(
                            id = "g_${safeId}_$index",
                            text = text,
                            rating = (review.rating ?: 4.0).roundToInt().coerceIn(1, 5),
                            category = ReviewCategory.FOOD
                        )
                    }
                LivePlaceDetails(priceLevel = priceLevel, reviews = mappedReviews)
            }
        }.getOrNull()
    }

    private fun PlacesSearchResult.toRestaurant(
        resolvedPriceLevel: Int?,
        liveReviews: List<Review>
    ): Restaurant {
        val addressText = formatted_address ?: vicinity
        val cityName = extractCity(addressText)
        val cuisineGuess = inferCuisine(types.orEmpty())
        val price = (resolvedPriceLevel ?: 2).coerceIn(1, 4)
        val ratingValue = (rating ?: 4.0).coerceIn(1.0, 5.0)
        val safeName = name?.takeIf { it.isNotBlank() } ?: "Unnamed Restaurant"
        val safePlaceId = place_id?.takeIf { it.isNotBlank() }
            ?: "gm_${safeName.lowercase().replace(" ", "_")}_${cityName.lowercase().replace(" ", "_")}"
        val vibeTags = buildList {
            add(if (ratingValue >= 4.3) "Popular" else "Hidden Gem")
            add(if (price >= 3) "Date Night" else "Casual")
            add(if (price <= 2) "Family" else "Modern")
        }.distinct()
        val liveMenuPreview = buildLiveMenuPreview(liveReviews, cuisineGuess)
            .ifEmpty { dishLookup[normalizeDishKey(safeName, cityName)].orEmpty() }

        return Restaurant(
            id = safePlaceId,
            name = safeName,
            city = cityName,
            cuisine = cuisineGuess,
            priceLevel = price,
            hasLivePriceLevel = resolvedPriceLevel != null,
            isAvgPriceEstimated = resolvedPriceLevel != null,
            vibeTags = vibeTags,
            photoLabels = emptyList(),
            menuPreview = liveMenuPreview,
            reviews = liveReviews,
            dishSentiments = emptyList(),
            avgPricePerPersonUsd = resolvedPriceLevel?.let { estimatedAveragePriceFromTier(it) }
        )
    }

    private fun normalizeDishKey(name: String, city: String): String {
        fun clean(s: String) = s.lowercase().replace(Regex("[^a-z0-9 ]"), "").trim()
        return "${clean(name)}|${clean(city)}"
    }

    private fun buildLiveMenuPreview(reviews: List<Review>, cuisine: String): List<String> {
        if (reviews.isEmpty()) return emptyList()

        val cuisineKeywords = cuisineSpecificKeywords(cuisine)
        val genericFallbackKeywords = setOf(
            "pizza", "pasta", "burger", "taco", "burrito", "ramen", "sushi", "sandwich", "salad",
            "steak", "fries", "wings", "noodles", "dumplings", "curry", "pho", "risotto", "paella",
            "falafel", "shawarma", "kebab", "gyoza", "tempura", "ceviche", "lasagna", "brisket",
            "ribs", "nachos", "quesadilla", "omelet", "pancakes", "waffles", "chowder", "soup"
        )
        val effectiveKeywords = if (cuisineKeywords.isNotEmpty()) cuisineKeywords + genericFallbackKeywords
            else genericFallbackKeywords

        val keywordCounts = mutableMapOf<String, Int>()

        reviews.forEach { review ->
            val tokens = review.text
                .lowercase()
                .replace(Regex("[^a-z\\s]"), " ")
                .split(Regex("\\s+"))
                .filter { it.length >= 3 }
                .distinct()

            tokens.forEach { token ->
                if (token in effectiveKeywords) {
                    keywordCounts[token] = (keywordCounts[token] ?: 0) + 1
                }
            }
        }

        val prioritized = keywordCounts.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { it.key }

        if (prioritized.isNotEmpty()) {
            return prioritized.map { it.replaceFirstChar { c -> c.uppercase() } }
        }

        // Only return words we recognise as actual food/dish keywords — never raw review tokens
        return emptyList()
    }

    private fun cuisineSpecificKeywords(cuisine: String): Set<String> {
        val c = cuisine.lowercase()
        return when {
            c.contains("japanese") || c.contains("sushi") ->
                setOf("ramen", "sushi", "sashimi", "udon", "tempura", "gyoza", "tonkatsu",
                    "edamame", "miso", "yakitori", "donburi", "onigiri", "takoyaki")
            c.contains("mexican") ->
                setOf("taco", "burrito", "enchilada", "quesadilla", "tamale", "pozole",
                    "mole", "carnitas", "guacamole", "nachos", "chilaquiles", "tostada")
            c.contains("italian") ->
                setOf("pizza", "pasta", "risotto", "lasagna", "gnocchi", "tiramisu",
                    "bruschetta", "ossobuco", "carbonara", "arancini", "panna", "focaccia")
            c.contains("chinese") ->
                setOf("dumplings", "noodles", "wonton", "dimsum", "peking", "mapo",
                    "xiaolongbao", "congee", "chow", "baozi", "hotpot", "kung")
            c.contains("indian") ->
                setOf("curry", "biryani", "naan", "tikka", "saag", "samosa",
                    "tandoori", "korma", "chutney", "dosa", "paneer", "vindaloo")
            c.contains("thai") ->
                setOf("pad", "satay", "larb", "massaman", "pho", "papaya",
                    "mango", "basil", "curry", "sticky", "som", "dumpling")
            c.contains("vietnamese") ->
                setOf("pho", "banh", "bun", "goi", "bo", "com",
                    "nem", "chao", "cha", "mi", "spring", "roll")
            c.contains("korean") ->
                setOf("bibimbap", "bulgogi", "kimchi", "tteokbokki", "samgyeopsal",
                    "japchae", "galbi", "sundubu", "jjigae", "banchan", "kimbap")
            c.contains("mediterranean") || c.contains("greek") ->
                setOf("falafel", "shawarma", "kebab", "hummus", "gyros", "tzatziki",
                    "souvlaki", "pita", "moussaka", "spanakopita", "dolma", "baklava")
            c.contains("american") ->
                setOf("burger", "steak", "wings", "fries", "ribs", "brisket",
                    "chowder", "mac", "waffles", "pancakes", "meatloaf", "coleslaw")
            c.contains("french") ->
                setOf("croissant", "quiche", "baguette", "crepe", "escargot",
                    "ratatouille", "bouillabaisse", "coq", "soufflé", "tarte", "cassoulet")
            c.contains("middle eastern") ->
                setOf("hummus", "falafel", "shawarma", "kebab", "tabbouleh",
                    "fattoush", "manakish", "kibbeh", "baklava", "kunafa", "ful")
            else -> emptySet()
        }
    }

    private fun estimatedAveragePriceFromTier(priceLevel: Int): Double {
        return when (priceLevel.coerceIn(1, 4)) {
            1 -> 12.0
            2 -> 24.0
            3 -> 45.0
            else -> 75.0
        }
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
        fun create(apiKey: String, dishLookup: Map<String, List<String>> = emptyMap()): GooglePlacesRestaurantApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return GooglePlacesRestaurantApiService(
                placesApi = retrofit.create(GooglePlacesApi::class.java),
                apiKey = apiKey,
                dishLookup = dishLookup
            )
        }
    }
}
