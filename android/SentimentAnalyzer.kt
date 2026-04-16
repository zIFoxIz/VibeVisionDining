package com.example.vibevision.ml

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlin.math.exp

/**
 * Sentiment Analyzer using Logistic Regression model
 * Handles text preprocessing, feature extraction, and inference
 */
class SentimentAnalyzer(
    private val modelJson: JsonObject,
    private val englishStopwords: Set<String>
) {

    data class PredictionResult(
        val sentiment: String,
        val label: Int,
        val confidence: Float,
        val scores: Map<String, Float>
    )

    private val classes: List<String> = modelJson.getAsJsonArray("classes").map { it.asString }
    private val coef: Array<FloatArray> = modelJson.getAsJsonArray("coef").map { row ->
        row.asJsonArray.map { it.asFloat }.toFloatArray()
    }.toTypedArray()
    private val intercept: FloatArray = modelJson.getAsJsonArray("intercept").map { it.asFloat }.toFloatArray()
    private val topFeatures: List<String> = modelJson.getAsJsonArray("top_features").map { it.asString }

    /**
     * Preprocess text: lowercase, tokenize, remove stopwords
     */
    fun preprocessText(text: String): List<String> {
        return text
            .lowercase()
            .split(Regex("[\\s\\p{P}]+"))  // Split on whitespace and punctuation
            .filter { token ->
                token.length > 1 && token !in englishStopwords
            }
    }

    /**
     * Extract TF-IDF-like features (simplified: term frequency in top features)
     */
    private fun extractFeatures(tokens: List<String>): FloatArray {
        val features = FloatArray(topFeatures.size)
        for (i in topFeatures.indices) {
            features[i] = tokens.count { it == topFeatures[i] }.toFloat()
        }
        return features
    }

    /**
     * Softmax activation for probability output
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val exps = logits.map { exp((it - logits.maxOrNull()!! ).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }

    /**
     * Predict sentiment from text
     */
    fun predict(text: String): PredictionResult {
        val tokens = preprocessText(text)
        if (tokens.isEmpty()) {
            return PredictionResult(
                sentiment = "neutral",
                label = 1,
                confidence = 0.33f,
                scores = mapOf(
                    "negative" to 0.33f,
                    "neutral" to 0.33f,
                    "positive" to 0.33f
                )
            )
        }

        val features = extractFeatures(tokens)

        // Logistic regression: compute logits
        val logits = FloatArray(classes.size) { classIdx ->
            var logit = intercept[classIdx]
            for (j in features.indices) {
                logit += coef[classIdx][j] * features[j]
            }
            logit
        }

        // Apply softmax for probabilities
        val probabilities = softmax(logits)

        // Get predicted class
        val predictedLabel = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val predictedSentiment = classes[predictedLabel]
        val confidence = probabilities[predictedLabel]

        return PredictionResult(
            sentiment = predictedSentiment,
            label = predictedLabel,
            confidence = confidence,
            scores = classes.indices.associate { i ->
                classes[i] to probabilities[i]
            }
        )
    }

    companion object {
        /**
         * Common English stopwords
         */
        fun getEnglishStopwords(): Set<String> {
            return setOf(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "from", "up", "about", "into", "through", "during",
                "before", "after", "above", "below", "between", "out", "off", "over", "under",
                "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
                "do", "does", "did", "will", "would", "could", "should", "may", "might", "must",
                "can", "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they",
                "what", "which", "who", "when", "where", "why", "how", "all", "each", "every",
                "some", "any", "no", "not", "only", "same", "so", "just", "as", "if", "then",
                "because", "as", "while", "although", "though", "if", "unless", "until", "once"
            )
        }
    }
}
