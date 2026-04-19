package com.example.vibevision

import com.example.vibevision.domain.SentimentAggregation
import com.example.vibevision.domain.VibeMatchEngine
import com.example.vibevision.ml.SentimentAnalyzer
import com.example.vibevision.model.DishSentiment
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.example.vibevision.model.VibePreference
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

class PerformanceBenchmarkTest {

    private fun createBenchmarkAnalyzer(): SentimentAnalyzer {
        val model = JsonObject().apply {
            add("classes", JsonArray().apply {
                add("negative")
                add("neutral")
                add("positive")
            })

            add("coef", JsonArray().apply {
                add(JsonArray().apply {
                    add(1.8f)
                    add(-1.4f)
                    add(-0.6f)
                    add(-1.1f)
                })
                add(JsonArray().apply {
                    add(0.2f)
                    add(0.2f)
                    add(0.1f)
                    add(0.2f)
                })
                add(JsonArray().apply {
                    add(-1.5f)
                    add(1.7f)
                    add(0.8f)
                    add(1.3f)
                })
            })

            add("intercept", JsonArray().apply {
                add(0.0f)
                add(0.0f)
                add(0.0f)
            })

            add("top_features", JsonArray().apply {
                add("bad")
                add("good")
                add("service")
                add("food")
            })
        }

        val stopwords = setOf("the", "and", "is", "a", "to")
        return SentimentAnalyzer(model, stopwords)
    }

    @Test
    fun sentimentPrediction_averageLatency_isFastEnough() {
        val analyzer = createBenchmarkAnalyzer()
        val samples = listOf(
            "good food and great service",
            "bad service but good location",
            "food is okay and service is okay",
            "excellent food and amazing staff",
            "bad food and bad service"
        )

        repeat(200) { analyzer.predict(samples[it % samples.size]) }

        val iterations = 1500
        val elapsedNs = measureNanoTime {
            repeat(iterations) {
                analyzer.predict(samples[it % samples.size])
            }
        }

        val avgMs = (elapsedNs / iterations.toDouble()) / 1_000_000.0
        println("Sentiment average latency: %.4f ms".format(avgMs))

        assertTrue("Expected average predict latency under 2.5ms but was %.4fms".format(avgMs), avgMs < 2.5)
    }

    @Test
    fun vibeMatch_averageLatency_isFastEnough() {
        val preferences = listOf(
            VibePreference("Cozy", true),
            VibePreference("Modern", true),
            VibePreference("Family", true),
            VibePreference("Romantic", false)
        )

        val reviews = List(50) { idx ->
            val rating = when {
                idx % 7 == 0 -> 2
                idx % 5 == 0 -> 3
                else -> 5
            }
            Review(id = "$idx", text = "review-$idx", rating = rating, category = ReviewCategory.FOOD)
        }

        repeat(200) {
            VibeMatchEngine.score(listOf("Cozy", "Modern", "Casual"), preferences, reviews)
        }

        val iterations = 3000
        val elapsedNs = measureNanoTime {
            repeat(iterations) {
                VibeMatchEngine.score(listOf("Cozy", "Modern", "Casual"), preferences, reviews)
            }
        }

        val avgMs = (elapsedNs / iterations.toDouble()) / 1_000_000.0
        println("Vibe match average latency: %.4f ms".format(avgMs))

        assertTrue("Expected average vibe-match latency under 1.2ms but was %.4fms".format(avgMs), avgMs < 1.2)
    }

    @Test
    fun sentimentAggregation_batchLatency_isFastEnough() {
        val dish = DishSentiment("Ramen", positive = 200, neutral = 40, negative = 20)

        val reviews = List(300) { idx ->
            Review(
                id = "r$idx",
                text = "review-$idx",
                rating = when {
                    idx % 9 == 0 -> 2
                    idx % 4 == 0 -> 3
                    else -> 5
                },
                category = ReviewCategory.SERVICE
            )
        }

        val restaurant = Restaurant(
            id = "perf",
            name = "Perf Kitchen",
            city = "Phoenix",
            cuisine = "Fusion",
            priceLevel = 3,
            vibeTags = listOf("Cozy", "Modern"),
            photoLabels = emptyList(),
            menuPreview = emptyList(),
            reviews = reviews,
            dishSentiments = listOf(dish)
        )

        repeat(200) {
            SentimentAggregation.aggregateDish(dish)
            SentimentAggregation.aggregateRestaurant(restaurant, reviews)
        }

        val iterations = 3000
        val elapsedNs = measureNanoTime {
            repeat(iterations) {
                SentimentAggregation.aggregateDish(dish)
                SentimentAggregation.aggregateRestaurant(restaurant, reviews)
            }
        }

        val avgMs = (elapsedNs / iterations.toDouble()) / 1_000_000.0
        println("Aggregation average latency: %.4f ms".format(avgMs))

        assertTrue("Expected aggregation average latency under 1.5ms but was %.4fms".format(avgMs), avgMs < 1.5)
    }
}
