package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.DishSentiment
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.DishCard
import com.example.vibevision.ui.components.DishCardVariant

@Composable
fun DishSentimentScreen(restaurant: Restaurant) {
    var sortBy by remember { mutableStateOf("Positive") }

    val (rawDishes, isEstimated) = remember(restaurant) { effectiveDishSentiments(restaurant) }

    val dishes = remember(rawDishes, sortBy) {
        when (sortBy) {
            "Negative" -> rawDishes.sortedByDescending { it.negative }
            "Mentioned" -> rawDishes.sortedByDescending { it.positive + it.neutral + it.negative }
            else -> rawDishes.sortedByDescending { it.positive }
        }
    }

    val winner = topDish(dishes)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Dish Sentiment Dashboard", fontWeight = FontWeight.Bold)
            Text(text = restaurant.name)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Sort Dishes", fontWeight = FontWeight.SemiBold)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Positive", "Negative", "Mentioned")) { option ->
                            AssistChip(
                                onClick = { sortBy = option },
                                label = { Text(option) },
                                leadingIcon = { Text(if (sortBy == option) "*" else "") }
                            )
                        }
                    }

                    if (isEstimated) {
                        Text(
                            text = "No live sentiment data — showing menu highlights with estimated scores.",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    if (winner != null) {
                        Text(text = "Top dish right now: ${winner.dishName}")
                    }
                }
            }
        }

        items(dishes) { dish ->
            DishCard(dish = dish, variant = DishCardVariant.HIGHLIGHT)
        }
    }
}

private fun topDish(dishes: List<DishSentiment>): DishSentiment? {
    return dishes.maxByOrNull { it.positive - it.negative }
}

private fun effectiveDishSentiments(restaurant: Restaurant): Pair<List<DishSentiment>, Boolean> {
    if (restaurant.dishSentiments.isNotEmpty()) return Pair(restaurant.dishSentiments, false)
    val estimated = restaurant.menuPreview.map { name ->
        DishSentiment(dishName = name, positive = 3, neutral = 2, negative = 1)
    }
    return Pair(estimated, estimated.isNotEmpty())
}
