package com.example.vibevision.domain

import com.example.vibevision.model.Review

object HeatmapCalculator {
    fun compute(reviews: List<Review>, vibeMatchScore: Float): Map<String, Float> {
        val total = reviews.size.coerceAtLeast(1).toFloat()
        val positive = reviews.count { it.rating >= 4 }.toFloat() / total
        val neutral = reviews.count { it.rating == 3 }.toFloat() / total
        val negative = reviews.count { it.rating <= 2 }.toFloat() / total

        val joy = (0.3f + positive * 0.55f + vibeMatchScore * 0.15f).coerceIn(0f, 1f)
        val calm = (0.2f + neutral * 0.5f + positive * 0.15f + vibeMatchScore * 0.15f).coerceIn(0f, 1f)
        val anger = (0.05f + negative * 0.7f - vibeMatchScore * 0.1f).coerceIn(0f, 1f)

        return mapOf(
            "joy" to joy,
            "calm" to calm,
            "anger" to anger
        )
    }
}
