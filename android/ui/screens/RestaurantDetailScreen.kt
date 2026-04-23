package com.example.vibevision.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.Review
import com.example.vibevision.model.ReviewCategory
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.components.DishCard
import com.example.vibevision.ui.components.DishCardVariant
import com.example.vibevision.ui.components.EmotionHeatmap
import com.example.vibevision.ui.components.ReviewCard
import com.example.vibevision.ui.components.ReviewCardVariant
import com.example.vibevision.ui.components.VibePreferenceChips

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    reviews: List<Review>,
    vibePreferences: List<VibePreference>,
    isFavorite: Boolean,
    vibeMatchScore: Float,
    vibeMatchDescription: String,
    heatmapScores: Map<String, Float>,
    aiSummary: String,
    timeline: List<String>,
    selectedShareTemplate: String,
    onFavoriteToggle: () -> Unit,
    onShareRestaurantCard: () -> Unit,
    onShareTemplateChange: (String) -> Unit,
    onOpenDishSentiment: () -> Unit,
    onSubmitReview: (String, Int, ReviewCategory) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOption by remember { mutableStateOf("Highest") }
    var draftReview by remember { mutableStateOf("") }
    var draftRating by remember { mutableStateOf("5") }
    var draftCategory by remember { mutableStateOf(ReviewCategory.FOOD) }
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var cameraMessage by remember { mutableStateOf<String?>(null) }
    val filteredReviews = filterAndSortReviews(reviews, selectedCategory, sortOption)

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedPhoto = bitmap
            cameraMessage = null
        } else {
            cameraMessage = "No photo captured."
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            takePhotoLauncher.launch(null)
        } else {
            cameraMessage = "Camera permission denied."
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Restaurant Detail", fontWeight = FontWeight.Bold)
            Text(text = "${restaurant.name} (${restaurant.city})", fontWeight = FontWeight.SemiBold)
            Text(text = "${restaurant.cuisine} • ${"$".repeat(restaurant.priceLevel)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onFavoriteToggle) {
                    Text(if (isFavorite) "Remove Favorite" else "Add Favorite")
                }
                Button(onClick = onShareRestaurantCard) {
                    Text("Share Restaurant Card")
                }
            }
            Button(onClick = onOpenDishSentiment) {
                Text("Open Dish Sentiment Dashboard")
            }
            Text(text = "Social Sharing Templates")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Quick", "Family Plan", "Date Night", "Foodie")) { template ->
                    AssistChip(
                        onClick = { onShareTemplateChange(template) },
                        label = { Text(template) },
                        leadingIcon = { Text(if (selectedShareTemplate == template) "*" else "") }
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Restaurant Photo Gallery", fontWeight = FontWeight.SemiBold)
                    Button(onClick = {
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            takePhotoLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Take Food/Restaurant Photo")
                    }

                    if (cameraMessage != null) {
                        Text(text = cameraMessage ?: "", color = Color.Gray)
                    }

                    capturedPhoto?.let { bitmap ->
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured restaurant photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(restaurant.photoLabels) { photo ->
                            Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    Text(text = photo)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Menu Preview Section", fontWeight = FontWeight.SemiBold)
                    restaurant.menuPreview.forEach { item ->
                        Text(text = "- $item")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Vibe Match System", fontWeight = FontWeight.SemiBold)
                    Text(text = "Current match: ${String.format("%.0f", vibeMatchScore * 100)}%")
                    Text(text = vibeMatchDescription)
                    VibePreferenceChips(preferences = vibePreferences, onToggle = {})
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "AI Sentiment Summary", fontWeight = FontWeight.SemiBold)
                    Text(text = aiSummary)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Restaurant Vibe Timeline", fontWeight = FontWeight.SemiBold)
                    timeline.forEach { point ->
                        Text(text = "- $point")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Emotion Heatmap", fontWeight = FontWeight.SemiBold)
                    EmotionHeatmap(scores = heatmapScores)
                }
            }
        }

        item {
            Text(text = "Dish Sentiment Breakdown", fontWeight = FontWeight.SemiBold)
        }

        items(restaurant.dishSentiments) { dish ->
            DishCard(dish = dish, variant = DishCardVariant.HIGHLIGHT)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Dish Rating Comparison", fontWeight = FontWeight.SemiBold)
                    val maxPositive = restaurant.dishSentiments.maxOfOrNull { it.positive }?.coerceAtLeast(1) ?: 1
                    restaurant.dishSentiments.forEach { dish ->
                        val ratio = dish.positive.toFloat() / maxPositive.toFloat()
                        Text(text = dish.dishName)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                                .height(12.dp)
                                .padding(bottom = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().height(12.dp)) {}
                        }
                        Text(text = "Positive votes: ${dish.positive}")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Review Sorting Options", fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Highest", "Lowest")) { option ->
                            AssistChip(
                                onClick = { sortOption = option },
                                label = { Text(option) },
                                leadingIcon = { Text(if (sortOption == option) "*" else "") }
                            )
                        }
                    }

                    Text(text = "Review Category Filters", fontWeight = FontWeight.SemiBold)
                    val categories = listOf("All") + ReviewCategory.entries.map { it.name.lowercase().replaceFirstChar { c -> c.titlecase() } }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            AssistChip(
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                leadingIcon = { Text(if (selectedCategory == category) "*" else "") }
                            )
                        }
                    }

                    filteredReviews.forEach { review ->
                        ReviewCard(review = review, variant = ReviewCardVariant.DETAILED)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "User Review Submission", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = draftReview,
                        onValueChange = { draftReview = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write your review") }
                    )
                    OutlinedTextField(
                        value = draftRating,
                        onValueChange = { draftRating = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Rating 1-5") }
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ReviewCategory.entries) { cat ->
                            AssistChip(
                                onClick = { draftCategory = cat },
                                label = { Text(cat.name) },
                                leadingIcon = { Text(if (draftCategory == cat) "*" else "") }
                            )
                        }
                    }
                    Button(onClick = {
                        val rating = draftRating.toIntOrNull()?.coerceIn(1, 5) ?: 5
                        onSubmitReview(draftReview, rating, draftCategory)
                        draftReview = ""
                        draftRating = "5"
                    }) {
                        Text("Submit Review")
                    }
                }
            }
        }

    }
}

private fun filterAndSortReviews(reviews: List<Review>, category: String, sort: String): List<Review> {
    val filtered = if (category == "All") {
        reviews
    } else {
        reviews.filter { it.category.name.equals(category, ignoreCase = true) }
    }

    return when (sort) {
        "Lowest" -> filtered.sortedBy { it.rating }
        else -> filtered.sortedByDescending { it.rating }
    }
}

