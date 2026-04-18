package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.RestaurantCard

@Composable
fun HomeFeedScreen(
    restaurants: List<Restaurant>,
    aiSummary: String,
    onRestaurantClick: (Restaurant) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "AI Sentiment Summary", fontWeight = FontWeight.SemiBold)
                    Text(text = aiSummary)
                }
            }
        }

        item {
            Text(text = "Home Feed", fontWeight = FontWeight.Bold)
        }

        items(restaurants) { restaurant ->
            RestaurantCard(restaurant = restaurant, onClick = onRestaurantClick)
        }
    }
}
