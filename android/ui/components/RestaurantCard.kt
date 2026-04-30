package com.example.vibevision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Restaurant
import com.example.vibevision.ui.theme.CardCompact
import com.example.vibevision.ui.theme.CardFeatured
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarmOrange
import java.util.Locale

enum class RestaurantCardVariant {
    DEFAULT,
    COMPACT,
    FEATURED
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: (Restaurant) -> Unit,
    isFavorite: Boolean = false,
    onFavoriteToggle: ((String) -> Unit)? = null,
    variant: RestaurantCardVariant = RestaurantCardVariant.DEFAULT
) {
    val containerColor = when (variant) {
        RestaurantCardVariant.FEATURED -> CardFeatured
        RestaurantCardVariant.COMPACT  -> CardCompact
        RestaurantCardVariant.DEFAULT  -> MaterialTheme.colorScheme.surface
    }
    val accentColor = when (variant) {
        RestaurantCardVariant.FEATURED -> WarmOrange
        RestaurantCardVariant.COMPACT  -> SageGreen
        RestaurantCardVariant.DEFAULT  -> Color.Transparent
    }
    val elevation = if (variant == RestaurantCardVariant.FEATURED) 4.dp else 2.dp
    val avgRating = if (restaurant.reviews.isEmpty()) null
    else restaurant.reviews.map { it.rating }.average()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(restaurant) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row {
            // Left accent bar (FEATURED / COMPACT only)
            if (accentColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(if (variant == RestaurantCardVariant.COMPACT) 68.dp else 110.dp)
                        .background(accentColor)
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Row 1 – Name + Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (onFavoriteToggle != null) {
                        IconButton(
                            onClick = { onFavoriteToggle(restaurant.id) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                                tint = if (isFavorite) Rose else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Row 2 – Cuisine • Price • City
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    if (variant != RestaurantCardVariant.COMPACT) {
                        Text("·", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(
                            text = formatPriceTier(restaurant),
                            style = MaterialTheme.typography.bodySmall,
                            color = SageGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("·", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(
                            text = formatPrice(restaurant),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = restaurant.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Row 3 – Star rating (DEFAULT / FEATURED only)
                if (variant != RestaurantCardVariant.COMPACT && avgRating != null) {
                    Row(
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Rating: ${String.format(Locale.US, "%.1f", avgRating)} out of 5, ${restaurant.reviews.size} reviews"
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = WarmOrange
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", avgRating),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "(${restaurant.reviews.size} reviews)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }

                // Row 4 – Top dish label (DEFAULT / FEATURED only)
                if (variant != RestaurantCardVariant.COMPACT) {
                    topDishesLabel(restaurant)?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Row 5 – Vibe tag chips
                if (restaurant.vibeTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        restaurant.vibeTags.take(4).forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = SageGreen.copy(alpha = 0.12f),
                                    labelColor = SageGreen
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    borderColor = SageGreen.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrice(restaurant: Restaurant): String {
    val avg = restaurant.avgPricePerPersonUsd
    return if (avg != null) {
        String.format(Locale.US, "$%.0f avg/person", avg)
    } else {
        "Price unavailable"
    }
}

private fun formatPriceTier(restaurant: Restaurant): String {
    if (restaurant.hasLivePriceLevel) return "$".repeat(restaurant.priceLevel)
    val avg = restaurant.avgPricePerPersonUsd
    return if (avg != null) {
        val estimated = when {
            avg < 15  -> 1
            avg < 30  -> 2
            avg < 60  -> 3
            else      -> 4
        }
        "~" + "$".repeat(estimated)
    } else {
        "$".repeat(2) // default mid-range guess
    }
}

private fun pricingConfidenceLabel(restaurant: Restaurant): String {
    if (!restaurant.hasLivePriceLevel) {
        return if (restaurant.avgPricePerPersonUsd != null)
            "Pricing confidence: Estimated from avg spend"
        else
            "Pricing confidence: Estimated (mid-range default)"
    }
    return when {
        restaurant.avgPricePerPersonUsd == null -> "Pricing confidence: Live tier"
        restaurant.isAvgPriceEstimated -> "Pricing confidence: Live tier and estimated avg"
        else -> "Pricing confidence: Live tier and verified avg"
    }
}

private fun topDishesLabel(restaurant: Restaurant): String? {
    val fromSentiment = restaurant.dishSentiments
        .sortedByDescending { it.positive }
        .take(2)
        .map { it.dishName }

    if (fromSentiment.isNotEmpty()) {
        return "Top dishes: ${fromSentiment.joinToString()}"
    }

    val fromMenu = restaurant.menuPreview.take(2)
    if (fromMenu.isNotEmpty()) {
        return "Menu highlights: ${fromMenu.joinToString()}"
    }

    return null
}
