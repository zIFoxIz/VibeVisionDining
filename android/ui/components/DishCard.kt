package com.example.vibevision.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.DishSentiment
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen

enum class DishCardVariant {
    STANDARD,
    HIGHLIGHT
}

@Composable
fun DishCard(dish: DishSentiment, variant: DishCardVariant = DishCardVariant.STANDARD) {
    val total = (dish.positive + dish.neutral + dish.negative).coerceAtLeast(1)
    val positiveRatio = dish.positive.toFloat() / total
    val neutralRatio  = dish.neutral.toFloat()  / total
    val negativeRatio = dish.negative.toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header row: dish name + optional sentiment icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dish.dishName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (variant == DishCardVariant.HIGHLIGHT) {
                    SentimentIcon(sentiment = if (dish.positive >= dish.negative) "positive" else "negative")
                }
            }

            Text(
                text = "$total mention${if (total == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )

            // Positive bar
            SentimentBar(
                label = "Loved",
                percent = positiveRatio,
                count = dish.positive,
                color = SageGreen
            )
            // Neutral bar
            SentimentBar(
                label = "Mixed",
                percent = neutralRatio,
                count = dish.neutral,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
            // Negative bar
            SentimentBar(
                label = "Disliked",
                percent = negativeRatio,
                count = dish.negative,
                color = Rose
            )
        }
    }
}

@Composable
private fun SentimentBar(label: String, percent: Float, count: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${String.format("%.0f", percent * 100)}%  ($count)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        LinearProgressIndicator(
            progress = percent,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}
