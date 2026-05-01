package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.vibevision.ui.theme.SageGreen

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
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Dish Sentiment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Sort by", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Positive", "Negative", "Mentioned")) { option ->
                            FilterChip(
                                selected = sortBy == option,
                                onClick = { sortBy = option },
                                label = { Text(option) },
                                leadingIcon = if (sortBy == option) {
                                    { Icon(Icons.Filled.Check, contentDescription = null) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = SageGreen,
                                    selectedLeadingIconColor = SageGreen
                                )
                            )
                        }
                    }

                    if (isEstimated) {
                        Text(
                            text = "No live sentiment data — showing menu highlights with estimated scores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    if (winner != null) {
                        Text(
                            text = "⭐ Most loved: ${winner.dishName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SageGreen
                        )
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
