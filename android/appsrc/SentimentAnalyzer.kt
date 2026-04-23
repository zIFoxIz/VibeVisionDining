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
) : ReviewSentimentPredictor {

    private val classes: List<String> = modelJson.getAsJsonArray("classes").map { it.asString }
    private val coef: Array<FloatArray> = modelJson.getAsJsonArray("coef").map { row ->
        row.asJsonArray.map { it.asFloat }.toFloatArray()
    }.toTypedArray()
    private val intercept: FloatArray = modelJson.getAsJsonArray("intercept").map { it.asFloat }.toFloatArray()
    private val topFeatures: List<String> = modelJson.getAsJsonArray("top_features").map { it.asString }
    private val topFeaturesSet: Set<String> = topFeatures.toHashSet()

    // Strong sentiment words absent from the trained model vocabulary
    private val strongPositiveWords = setOf(
        "exceptional", "outstanding", "phenomenal", "superb", "magnificent",
        "excellent", "splendid", "terrific", "brilliant", "flawless", "extraordinary",
        "marvelous", "spectacular", "stellar", "impeccable", "exquisite"
    )
    private val strongNegativeWords = setOf(
        "disgusting", "revolting", "atrocious", "abysmal", "dreadful",
        "appalling", "horrendous", "inedible", "deplorable", "despicable",
        "nauseating", "putrid", "repulsive", "vile", "loathsome"
    )

    /**
     * Preprocess text: lowercase, tokenize, remove stopwords; also produces bigrams
     * so that multi-word model features (e.g. "great service") can match.
     */
    fun preprocessText(text: String): List<String> {
        val tokens = text
            .lowercase()
            .split(Regex("[\\s\\p{P}]+"))
            .filter { token -> token.length > 1 && token !in englishStopwords }
        val bigrams = tokens.zipWithNext { a, b -> "$a $b" }
        return tokens + bigrams
    }

    /**
     * Extract TF-IDF-like features (term frequency against top features list).
     */
    private fun extractFeatures(tokens: List<String>): FloatArray {
        val features = FloatArray(topFeatures.size)
        for (i in topFeatures.indices) {
            features[i] = tokens.count { it == topFeatures[i] }.toFloat()
        }
        return features
    }

    /**
     * If the model returns a low-confidence or neutral result but the raw text
     * contains strong unambiguous sentiment words, override the prediction.
     */
    private fun applyKeywordOverride(
        text: String,
        modelSentiment: String,
        modelConfidence: Float,
        scores: MutableMap<String, Float>
    ): Pair<String, Float> {
        val lower = text.lowercase()
        val hasStrongPositive = strongPositiveWords.any { lower.contains(it) }
        val hasStrongNegative = strongNegativeWords.any { lower.contains(it) }

        // Only override when the model is uncertain or contradicts obvious signals
        if (hasStrongPositive && !hasStrongNegative && modelSentiment != "positive") {
            scores["positive"] = maxOf(scores["positive"] ?: 0f, 0.72f)
            scores["neutral"] = minOf(scores["neutral"] ?: 0f, 0.20f)
            scores["negative"] = minOf(scores["negative"] ?: 0f, 0.08f)
            return "positive" to (scores["positive"] ?: 0.72f)
        }
        if (hasStrongNegative && !hasStrongPositive && modelSentiment != "negative") {
            scores["negative"] = maxOf(scores["negative"] ?: 0f, 0.72f)
            scores["neutral"] = minOf(scores["neutral"] ?: 0f, 0.20f)
            scores["positive"] = minOf(scores["positive"] ?: 0f, 0.08f)
            return "negative" to (scores["negative"] ?: 0.72f)
        }
        return modelSentiment to modelConfidence
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
    override fun predict(text: String): PredictionResult {
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

        val mutableScores = classes.indices.associate { i -> classes[i] to probabilities[i] }.toMutableMap()
        val (finalSentiment, finalConfidence) = applyKeywordOverride(text, predictedSentiment, confidence, mutableScores)

        return PredictionResult(
            sentiment = finalSentiment,
            label = classes.indexOf(finalSentiment).takeIf { it >= 0 } ?: predictedLabel,
            confidence = finalConfidence,
            scores = mutableScores
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
