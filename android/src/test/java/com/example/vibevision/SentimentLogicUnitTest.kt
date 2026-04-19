package com.example.vibevision

import com.example.vibevision.domain.EmojiSentimentMapper
import com.example.vibevision.domain.SentimentAggregation
import com.example.vibevision.ml.SentimentAnalyzer
import com.example.vibevision.model.DishSentiment
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentimentLogicUnitTest {

    private fun createTestAnalyzer(): SentimentAnalyzer {
        val model = JsonObject().apply {
            add("classes", JsonArray().apply {
                add("negative")
                add("neutral")
                add("positive")
            })

            add("coef", JsonArray().apply {
                // negative: bad high, good low
                add(JsonArray().apply {
                    add(2.0f)
                    add(-2.0f)
                })
                // neutral: both weak
                add(JsonArray().apply {
                    add(0.1f)
                    add(0.1f)
                })
                // positive: good high, bad low
                add(JsonArray().apply {
                    add(-2.0f)
                    add(2.0f)
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
            })
        }

        val stopwords = setOf("the", "and", "is")
        return SentimentAnalyzer(model, stopwords)
    }

    @Test
    fun preprocessText_removesStopwordsAndPunctuation() {
        val analyzer = createTestAnalyzer()

        val tokens = analyzer.preprocessText("The food is good, and service is good!")

        assertEquals(listOf("food", "good", "service", "good"), tokens)
    }

    @Test
    fun predict_returnsPositiveForPositiveInput() {
        val analyzer = createTestAnalyzer()

        val result = analyzer.predict("good good dinner")

        assertEquals("positive", result.sentiment)
        assertTrue(result.confidence > 0.6f)
    }

    @Test
    fun predict_returnsNeutralFallbackForBlankInput() {
        val analyzer = createTestAnalyzer()

        val result = analyzer.predict("the and is")

        assertEquals("neutral", result.sentiment)
        assertEquals(1, result.label)
        assertEquals(0.33f, result.confidence, 0.001f)
    }

    @Test
    fun emojiMapper_returnsExpectedEmojiBySentimentAndConfidence() {
        assertEquals("😁", EmojiSentimentMapper.emojiFor("positive", 0.9f))
        assertEquals("🙂", EmojiSentimentMapper.emojiFor("positive", 0.5f))
        assertEquals("😠", EmojiSentimentMapper.emojiFor("negative", 0.8f))
        assertEquals("🤔", EmojiSentimentMapper.emojiFor("neutral", 0.4f))
    }

    @Test
    fun sentimentAggregation_computesDishAndRestaurantMetrics() {
        val dish = DishSentiment(
            dishName = "Ramen",
            positive = 8,
            neutral = 1,
            negative = 1
        )

        val dishAgg = SentimentAggregation.aggregateDish(dish)
        assertEquals(10, dishAgg.totalMentions)
        assertEquals(0.8f, dishAgg.positiveRatio, 0.001f)

        val reviews = listOf(
            Review("1", "Great", 5, ReviewCategory.FOOD),
            Review("2", "Okay", 3, ReviewCategory.SERVICE),
            Review("3", "Bad", 2, ReviewCategory.VALUE)
        )

        val restaurant = Restaurant(
            id = "r1",
            name = "Test",
            city = "Phoenix",
            cuisine = "Asian",
            priceLevel = 2,
            vibeTags = listOf("Cozy"),
            photoLabels = emptyList(),
            menuPreview = emptyList(),
            reviews = reviews,
            dishSentiments = listOf(dish)
        )

        val restAgg = SentimentAggregation.aggregateRestaurant(restaurant, reviews)
        assertEquals(3.33f, restAgg.averageRating, 0.01f)
        assertEquals("positive", restAgg.dominantSentiment)
        assertEquals(1f / 3f, restAgg.neutralReviewRatio, 0.001f)
    }
}
