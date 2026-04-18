package com.example.vibevision.data

import com.example.vibevision.model.DishSentiment
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review

object SampleRestaurantData {
    val restaurants: List<Restaurant> = listOf(
        Restaurant(
            id = "r1",
            name = "Ember & Basil",
            cuisine = "Italian",
            priceLevel = 3,
            vibeTags = listOf("Cozy", "Date Night", "Romantic"),
            reviews = listOf(
                Review("rv1", "Great pasta, warm service, and beautiful atmosphere.", 5),
                Review("rv2", "Food was decent but wait time was too long.", 3),
                Review("rv3", "Loved the truffle gnocchi, will come back.", 5)
            ),
            dishSentiments = listOf(
                DishSentiment("Truffle Gnocchi", 22, 4, 2),
                DishSentiment("Margherita Pizza", 16, 6, 3),
                DishSentiment("Tiramisu", 14, 3, 1)
            )
        ),
        Restaurant(
            id = "r2",
            name = "Citrus Lantern",
            cuisine = "Asian Fusion",
            priceLevel = 2,
            vibeTags = listOf("Energetic", "Friends", "Modern"),
            reviews = listOf(
                Review("rv4", "Flavorful ramen and fast service.", 4),
                Review("rv5", "Cool place, but music was a bit loud.", 3),
                Review("rv6", "Amazing bao buns and great cocktails.", 5)
            ),
            dishSentiments = listOf(
                DishSentiment("Spicy Ramen", 18, 5, 4),
                DishSentiment("Bao Buns", 20, 3, 2),
                DishSentiment("Mango Mochi", 11, 4, 1)
            )
        ),
        Restaurant(
            id = "r3",
            name = "Willow Market Kitchen",
            cuisine = "American",
            priceLevel = 2,
            vibeTags = listOf("Family", "Casual", "Comfort"),
            reviews = listOf(
                Review("rv7", "Great burgers and friendly staff.", 4),
                Review("rv8", "Portions are good, fries were cold.", 2),
                Review("rv9", "Perfect brunch spot for weekends.", 5)
            ),
            dishSentiments = listOf(
                DishSentiment("Classic Burger", 19, 5, 3),
                DishSentiment("Buttermilk Pancakes", 15, 6, 2),
                DishSentiment("Loaded Fries", 12, 7, 4)
            )
        )
    )
}
