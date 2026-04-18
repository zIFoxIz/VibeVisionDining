package com.example.vibevision.model

data class Review(
    val id: String,
    val text: String,
    val rating: Int
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
    val cuisine: String,
    val priceLevel: Int,
    val vibeTags: List<String>,
    val reviews: List<Review>,
    val dishSentiments: List<DishSentiment>
)

data class VibePreference(
    val vibe: String,
    val enabled: Boolean
)
