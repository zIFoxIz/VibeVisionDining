package com.example.vibevision.data

import com.example.vibevision.ml.PredictionResult
import com.example.vibevision.ml.ReviewSentimentPredictor

object ReviewParser {
    fun extractDishMentions(text: String, knownDishes: List<String>): List<String> {
        val lower = text.lowercase()
        return knownDishes.filter { dish -> lower.contains(dish.lowercase()) }
    }

    fun classifyReview(text: String, analyzer: ReviewSentimentPredictor): PredictionResult {
        return analyzer.predict(text)
    }
}
