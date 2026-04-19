package com.example.vibevision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vibevision.ui.theme.ErrorRed
import com.example.vibevision.ui.theme.NeutralGray
import com.example.vibevision.ui.theme.SuccessGreen

@Composable
fun SentimentIcon(sentiment: String) {
    val (label, color) = when (sentiment.lowercase()) {
        "positive" -> "POS" to SuccessGreen
        "negative" -> "NEG" to ErrorRed
        else -> "NEU" to NeutralGray
    }

    Box(
        modifier = Modifier
            .background(color = color, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White)
    }
}
