package com.example.vibevision.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.theme.CardCompact
import com.example.vibevision.ui.theme.CardFeatured

enum class RestaurantCardVariant {
    DEFAULT,
    COMPACT,
    FEATURED
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: (Restaurant) -> Unit,
    isFavorite: Boolean = false,
    onFavoriteToggle: ((String) -> Unit)? = null,
    variant: RestaurantCardVariant = RestaurantCardVariant.DEFAULT
) {
    val cardModifier = when (variant) {
        RestaurantCardVariant.FEATURED -> Modifier.background(CardFeatured)
        RestaurantCardVariant.COMPACT -> Modifier.background(CardCompact)
        RestaurantCardVariant.DEFAULT -> Modifier
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(restaurant) }
            .then(cardModifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = restaurant.name, fontWeight = FontWeight.Bold)
                if (onFavoriteToggle != null) {
                    TextButton(onClick = { onFavoriteToggle(restaurant.id) }) {
                        Text(if (isFavorite) "Unfav" else "Fav")
                    }
                }
            }
            if (variant != RestaurantCardVariant.COMPACT) {
                Text(text = "${restaurant.cuisine} • ${"$".repeat(restaurant.priceLevel)}")
            }
            Text(text = restaurant.city)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                restaurant.vibeTags.take(3).forEach { tag ->
                    Text(text = "#$tag")
                }
            }
        }
    }
}
