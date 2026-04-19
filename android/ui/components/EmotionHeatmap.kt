package com.example.vibevision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EmotionHeatmap(scores: Map<String, Float>) {
    val ordered = listOf("joy", "calm", "neutral", "frustration", "anger", "trust")
    val values = ordered.associateWith { scores[it] ?: 0f }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Heatmap Grid", fontWeight = FontWeight.SemiBold)

        ordered.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { key ->
                    val value = values[key] ?: 0f
                    val alpha = 0.2f + value.coerceIn(0f, 1f) * 0.8f
                    val color = when (key) {
                        "joy" -> Color(0xFF4CAF50)
                        "calm" -> Color(0xFF03A9F4)
                        "neutral" -> Color(0xFF9E9E9E)
                        "frustration" -> Color(0xFFFF9800)
                        "trust" -> Color(0xFF8BC34A)
                        else -> Color(0xFFF44336)
                    }.copy(alpha = alpha)

                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(60.dp)
                            .background(color)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${key.uppercase()}\n${String.format("%.0f", value * 100)}%")
                    }
                }
            }
        }
    }
}
