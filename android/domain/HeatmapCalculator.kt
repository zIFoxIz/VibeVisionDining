package com.example.vibevision.domain

import com.example.vibevision.model.Review

object HeatmapCalculator {
    fun compute(reviews: List<Review>, vibeMatchScore: Float): Map<String, Float> {
        val total = reviews.size.coerceAtLeast(1).toFloat()
        val positive = reviews.count { it.rating >= 4 }.toFloat() / total
        val neutral = reviews.count { it.rating == 3 }.toFloat() / total
        val negative = reviews.count { it.rating <= 2 }.toFloat() / total

        val joy = (0.35f + positive * 0.5f + vibeMatchScore * 0.15f).coerceIn(0f, 1f)
        val calm = (0.25f + neutral * 0.35f + vibeMatchScore * 0.2f).coerceIn(0f, 1f)
        val frustration = (0.1f + negative * 0.55f - vibeMatchScore * 0.1f).coerceIn(0f, 1f)
        val anger = (0.05f + negative * 0.45f - vibeMatchScore * 0.1f).coerceIn(0f, 1f)

        return mapOf(
            "joy" to joy,
            "calm" to calm,
            "neutral" to neutral.coerceIn(0f, 1f),
            "frustration" to frustration,
            "anger" to anger
        )
    }
}
