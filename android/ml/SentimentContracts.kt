package com.example.vibevision.ml

data class PredictionResult(
    val sentiment: String,
    val label: Int,
    val confidence: Float,
    val scores: Map<String, Float>
)

interface ReviewSentimentPredictor {
    fun predict(text: String): PredictionResult
}
