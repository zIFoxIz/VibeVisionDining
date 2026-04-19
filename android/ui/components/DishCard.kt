package com.example.vibevision.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.DishSentiment

enum class DishCardVariant {
    STANDARD,
    HIGHLIGHT
}

@Composable
fun DishCard(dish: DishSentiment, variant: DishCardVariant = DishCardVariant.STANDARD) {
    val total = (dish.positive + dish.neutral + dish.negative).coerceAtLeast(1)
    val positiveRatio = dish.positive.toFloat() / total.toFloat()
    val neutralRatio = dish.neutral.toFloat() / total.toFloat()
    val negativeRatio = dish.negative.toFloat() / total.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = dish.dishName, fontWeight = FontWeight.SemiBold)
            if (variant == DishCardVariant.HIGHLIGHT) {
                SentimentIcon(sentiment = if (dish.positive >= dish.negative) "positive" else "negative")
            }
            Text(text = "$total mentions")

            Text(text = "Positive ${String.format("%.0f", positiveRatio * 100)}%")
            LinearProgressIndicator(progress = { positiveRatio }, modifier = Modifier.fillMaxWidth())

            Text(text = "Neutral ${String.format("%.0f", neutralRatio * 100)}%")
            LinearProgressIndicator(progress = { neutralRatio }, modifier = Modifier.fillMaxWidth())

            Text(text = "Negative ${String.format("%.0f", negativeRatio * 100)}%")
            LinearProgressIndicator(progress = { negativeRatio }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "POS ${dish.positive}")
                Text(text = "NEU ${dish.neutral}")
                Text(text = "NEG ${dish.negative}")
            }
        }
    }
}
