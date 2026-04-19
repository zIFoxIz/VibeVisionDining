package com.example.vibevision.domain

object EmojiSentimentMapper {
    fun emojiFor(sentiment: String, confidence: Float): String {
        return when (sentiment.lowercase()) {
            "positive" -> if (confidence >= 0.75f) "😁" else "🙂"
            "negative" -> if (confidence >= 0.75f) "😠" else "🙁"
            else -> if (confidence >= 0.75f) "😐" else "🤔"
        }
    }
}
