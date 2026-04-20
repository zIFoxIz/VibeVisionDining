package com.example.vibevision.model

enum class ReviewCategory {
    FOOD,
    SERVICE,
    ATMOSPHERE,
    VALUE
}

enum class LanguageOption {
    ENGLISH,
    SPANISH,
    FRENCH
}

data class Review(
    val id: String,
    val text: String,
    val rating: Int,
    val category: ReviewCategory
)

data class DishSentiment(
    val dishName: String,
    val positive: Int,
    val neutral: Int,
    val negative: Int
)

data class Restaurant(
    val id: String,
    val name: String,
    val city: String,
    val cuisine: String,
    val priceLevel: Int,
    val vibeTags: List<String>,
    val photoLabels: List<String>,
    val menuPreview: List<String>,
    val reviews: List<Review>,
    val dishSentiments: List<DishSentiment>
)

data class VibePreference(
    val vibe: String,
    val enabled: Boolean
)

data class UserProfile(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = ""
)
