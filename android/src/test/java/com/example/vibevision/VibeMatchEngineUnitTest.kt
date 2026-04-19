package com.example.vibevision

import com.example.vibevision.domain.VibeMatchEngine
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.example.vibevision.model.VibePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VibeMatchEngineUnitTest {

    @Test
    fun score_returnsZero_whenNoEnabledPreferences() {
        val preferences = listOf(
            VibePreference("Cozy", false),
            VibePreference("Modern", false)
        )

        val score = VibeMatchEngine.score(
            vibeTags = listOf("Cozy", "Modern"),
            preferences = preferences,
            reviews = emptyList()
        )

        assertEquals(0f, score, 0.0001f)
    }

    @Test
    fun score_isCaseInsensitive_andIncludesReviewBoost() {
        val preferences = listOf(
            VibePreference("cozy", true),
            VibePreference("Modern", true)
        )

        val reviews = listOf(
            Review("1", "Great", 5, ReviewCategory.FOOD),
            Review("2", "Awesome", 5, ReviewCategory.SERVICE)
        )

        val score = VibeMatchEngine.score(
            vibeTags = listOf("COZY", "romantic"),
            preferences = preferences,
            reviews = reviews
        )

        // overlapScore = 1/2 = 0.5 -> weighted = 0.4
        // reviewBoost = (5/5)*0.2 = 0.2
        // final = 0.6
        assertEquals(0.6f, score, 0.0001f)
    }

    @Test
    fun score_isClampedToOne() {
        val preferences = listOf(
            VibePreference("Cozy", true)
        )

        val reviews = listOf(
            Review("1", "Perfect", 5, ReviewCategory.FOOD)
        )

        val score = VibeMatchEngine.score(
            vibeTags = listOf("Cozy", "Cozy", "Cozy"),
            preferences = preferences,
            reviews = reviews
        )

        assertTrue(score <= 1f)
        assertEquals(1f, score, 0.0001f)
    }

    @Test
    fun explain_returnsExpectedBuckets() {
        assertEquals("Excellent match (80%)", VibeMatchEngine.explain(0.8f))
        assertEquals("Strong match (60%)", VibeMatchEngine.explain(0.6f))
        assertEquals("Moderate match (40%)", VibeMatchEngine.explain(0.4f))
        assertEquals("Weak match (39%)", VibeMatchEngine.explain(0.39f))
    }
}
