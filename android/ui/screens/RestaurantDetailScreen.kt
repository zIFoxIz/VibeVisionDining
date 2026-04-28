package com.example.vibevision.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.components.VibePreferenceChips
import com.example.vibevision.ui.theme.InkBlue
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarmOrange

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
            SectionHeader(
                title = restaurant.name,
                subtitle = "${restaurant.cuisine}  ·  ${"$".repeat(restaurant.priceLevel)}  ·  ${restaurant.city}"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favourite toggle
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFavorite) Rose.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove favourite" else "Add favourite",
                        tint = if (isFavorite) Rose else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                // Share
                OutlinedButton(
                    onClick = onShareRestaurantCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Share Card", style = MaterialTheme.typography.labelLarge)
                }
                // Dish Sentiment
                Button(
                    onClick = onOpenDishSentiment,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Dishes", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Share as",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Quick", "Family Plan", "Date Night", "Foodie")) { template ->
                        FilterChip(
                            selected = selectedShareTemplate == template,
                            onClick = { onShareTemplateChange(template) },
                            label = { Text(template, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "📷 Photo Gallery", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = {
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) takePhotoLauncher.launch(null)
                            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" Take a Photo", style = MaterialTheme.typography.labelLarge)
                    }
                    if (cameraMessage != null) {
                        Text(
                            text = cameraMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    capturedPhoto?.let { bitmap ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured restaurant photo",
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (restaurant.photoLabels.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(restaurant.photoLabels) { photo ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.08f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        text = photo,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🍴 Menu Highlights", style = MaterialTheme.typography.titleMedium)
                    if (restaurant.menuPreview.isEmpty()) {
                        Text(
                            text = "No menu highlights available yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        restaurant.menuPreview.forEach { menuItem ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(SageGreen)
                                )
                                Text(
                                    text = menuItem,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨ Vibe Match", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${String.format("%.0f", vibeMatchScore * 100)}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = SageGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = vibeMatchScore.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth(),
                        color = SageGreen,
                        trackColor = SageGreen.copy(alpha = 0.15f)
                    )
                    Text(
                        text = vibeMatchDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    VibePreferenceChips(preferences = vibePreferences, onToggle = {})
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(colors = listOf(InkBlue, SageGreen)))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🧠 AI Sentiment Summary",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = aiSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🌡️ Emotion Heatmap", style = MaterialTheme.typography.titleMedium)
                    EmotionHeatmap(scores = heatmapScores)
                }
            }
        }

        if (restaurant.dishSentiments.isNotEmpty()) {
            item {
                SectionHeader(title = "Dish Sentiment", subtitle = "How diners feel about each dish")
            }

            items(restaurant.dishSentiments) { dish ->
                DishCard(dish = dish, variant = DishCardVariant.HIGHLIGHT)
            }
        }

        if (restaurant.dishSentiments.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "👍 Positive Votes", style = MaterialTheme.typography.titleMedium)
                        val maxPositive = restaurant.dishSentiments.maxOfOrNull { it.positive }?.coerceAtLeast(1) ?: 1
                        restaurant.dishSentiments.forEach { dish ->
                            val ratio = dish.positive.toFloat() / maxPositive.toFloat()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dish.dishName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${dish.positive} 👍",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageGreen
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = ratio.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = SageGreen,
                                    trackColor = SageGreen.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Reviews", subtitle = "What diners are saying")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Leave a Review", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = draftReview,
                        onValueChange = { draftReview = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Share your experience…") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )

                    // Star rating picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Rating:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        (1..5).forEach { star ->
                            val selected = (draftRating.toIntOrNull() ?: 5) >= star
                            IconButton(
                                onClick = { draftRating = star.toString() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "$star stars",
                                    tint = if (selected) WarmOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ReviewCategory.entries) { cat ->
                            FilterChip(
                                selected = draftCategory == cat,
                                onClick = { draftCategory = cat },
                                label = {
                                    Text(
                                        cat.name.lowercase().replaceFirstChar { it.titlecase() },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val rating = draftRating.toIntOrNull()?.coerceIn(1, 5) ?: 5
                            onSubmitReview(draftReview, rating, draftCategory)
                            draftReview = ""
                            draftRating = "5"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Review", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort
                Text(
                    text = "Sort:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                listOf("Highest", "Lowest").forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        label = { Text(option, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            val categories = listOf("All") + ReviewCategory.entries.map {
                it.name.lowercase().replaceFirstChar { c -> c.titlecase() }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        items(filteredReviews) { review ->
            ReviewCard(review = review, variant = ReviewCardVariant.DETAILED)
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

