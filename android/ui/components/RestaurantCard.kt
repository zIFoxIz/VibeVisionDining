package com.example.vibevision.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: (Restaurant) -> Unit,
    isFavorite: Boolean = false,
    onFavoriteToggle: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(restaurant) },
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
            Text(text = "${restaurant.cuisine} • ${"$".repeat(restaurant.priceLevel)}")
            Text(text = restaurant.city)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                restaurant.vibeTags.take(3).forEach { tag ->
                    Text(text = "#$tag")
                }
            }
        }
    }
}
