package com.example.vibevision.domain

import com.example.vibevision.model.Review
import com.example.vibevision.model.VibePreference

object VibeMatchEngine {
    fun score(vibeTags: List<String>, preferences: List<VibePreference>, reviews: List<Review>): Float {
        val enabledPrefs = preferences.filter { it.enabled }.map { it.vibe.lowercase() }.toSet()
        if (enabledPrefs.isEmpty()) return 0f

        val overlap = vibeTags.count { tag -> enabledPrefs.contains(tag.lowercase()) }
        val overlapScore = overlap.toFloat() / enabledPrefs.size.toFloat()

        val avgRating = if (reviews.isEmpty()) 0f else reviews.map { it.rating }.average().toFloat()
        val reviewBoost = (avgRating / 5f) * 0.2f

        return (overlapScore * 0.8f + reviewBoost).coerceIn(0f, 1f)
    }

    fun explain(score: Float): String {
        val percent = (score * 100).toInt()
        val bucket = when {
            score >= 0.8f -> "Excellent match"
            score >= 0.6f -> "Strong match"
            score >= 0.4f -> "Moderate match"
            else -> "Weak match"
        }
        return "$bucket ($percent%)"
    }
}
