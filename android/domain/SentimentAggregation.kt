package com.example.vibevision.domain

import com.example.vibevision.model.DishSentiment
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review

data class DishSentimentAggregate(
    val totalMentions: Int,
    val positiveRatio: Float,
    val neutralRatio: Float,
    val negativeRatio: Float
)

data class RestaurantSentimentAggregate(
    val averageRating: Float,
    val positiveReviewRatio: Float,
    val neutralReviewRatio: Float,
    val negativeReviewRatio: Float,
    val dominantSentiment: String
)

object SentimentAggregation {
    fun aggregateDish(dish: DishSentiment): DishSentimentAggregate {
        val total = (dish.positive + dish.neutral + dish.negative).coerceAtLeast(1)
        return DishSentimentAggregate(
            totalMentions = total,
            positiveRatio = dish.positive.toFloat() / total,
            neutralRatio = dish.neutral.toFloat() / total,
            negativeRatio = dish.negative.toFloat() / total
        )
    }

    fun aggregateRestaurant(restaurant: Restaurant, reviews: List<Review>): RestaurantSentimentAggregate {
        val total = reviews.size.coerceAtLeast(1)
        val positive = reviews.count { it.rating >= 4 }
        val neutral = reviews.count { it.rating == 3 }
        val negative = reviews.count { it.rating <= 2 }
        val avg = if (reviews.isEmpty()) 0f else reviews.map { it.rating }.average().toFloat()

        val dominant = when {
            positive >= neutral && positive >= negative -> "positive"
            negative >= positive && negative >= neutral -> "negative"
            else -> "neutral"
        }

        return RestaurantSentimentAggregate(
            averageRating = avg,
            positiveReviewRatio = positive.toFloat() / total,
            neutralReviewRatio = neutral.toFloat() / total,
            negativeReviewRatio = negative.toFloat() / total,
            dominantSentiment = dominant
        )
    }
}
