package com.example.vibevision.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SentimentIcon(sentiment: String) {
    val label = when (sentiment.lowercase()) {
        "positive" -> "POS"
        "negative" -> "NEG"
        else -> "NEU"
    }
    Text(text = label)
}
