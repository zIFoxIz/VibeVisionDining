package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.components.RestaurantCard

@Composable
fun RestaurantSearchScreen(
    query: String,
    restaurants: List<Restaurant>,
    onQueryChange: (String) -> Unit,
    onRestaurantClick: (Restaurant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Restaurant Search", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name, cuisine, or vibe") }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(restaurants) { restaurant ->
                RestaurantCard(restaurant = restaurant, onClick = onRestaurantClick)
            }
        }
    }
}
