package com.example.vibevision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EmotionHeatmap(scores: Map<String, Float>) {
    val ordered = listOf("joy", "calm", "neutral", "frustration", "anger")
    val values = ordered.associateWith { scores[it] ?: 0f }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Emotion Heatmap", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ordered.forEach { key ->
                val value = values[key] ?: 0f
                val alpha = 0.2f + value.coerceIn(0f, 1f) * 0.8f
                val color = when (key) {
                    "joy" -> Color(0xFF4CAF50)
                    "calm" -> Color(0xFF03A9F4)
                    "neutral" -> Color(0xFF9E9E9E)
                    "frustration" -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }.copy(alpha = alpha)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = key.take(3).uppercase())
                    Row(
                        modifier = Modifier
                            .width(42.dp)
                            .height(42.dp)
                            .background(color)
                    ) {}
                }
            }
        }
    }
}
