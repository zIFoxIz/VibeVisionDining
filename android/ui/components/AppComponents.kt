package com.example.vibevision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.Review
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.theme.OverlayScrim
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarningOrange

data class NavDestinationItem(
    val route: String,
    val label: String
)

enum class ReviewCardVariant {
    COMPACT,
    DETAILED
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(text = subtitle)
        }
    }
}

@Composable
fun MetricCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = value)
        }
    }
}

@Composable
fun EmptyStateCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = message)
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun BrandLogoMark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(SageGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "V", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(text = "VibeVision", fontWeight = FontWeight.Bold)
            Text(text = "Dining", color = WarningOrange)
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    items: List<NavDestinationItem>,
    selectedRoute: String?,
    onNavigate: (NavDestinationItem) -> Unit
) {
    NavigationBar {
        items.forEach { destination ->
            NavigationBarItem(
                selected = selectedRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = { Text(text = destination.label.take(1)) },
                label = { Text(text = destination.label) }
            )
        }
    }
}

@Composable
fun FilterChipRow(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                AssistChip(
                    onClick = { onToggle(option) },
                    label = { Text(option) },
                    leadingIcon = { Text(if (selected.contains(option)) "*" else "") }
                )
            }
        }
    }
}

@Composable
fun VibePreferenceChips(
    preferences: List<VibePreference>,
    onToggle: (String) -> Unit
) {
    FilterChipRow(
        title = "Vibe Preference Chips",
        options = preferences.map { it.vibe },
        selected = preferences.filter { it.enabled }.map { it.vibe }.toSet(),
        onToggle = onToggle
    )
}

@Composable
fun ReviewCard(review: Review, variant: ReviewCardVariant = ReviewCardVariant.DETAILED) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "${review.rating}/5 • ${review.category.name}", fontWeight = FontWeight.SemiBold)
            if (variant == ReviewCardVariant.DETAILED) {
                Text(text = review.text)
            } else {
                Text(text = review.text.take(72) + if (review.text.length > 72) "..." else "")
            }
        }
    }
}

@Composable
fun OverlayPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(OverlayScrim)
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = Rose, textAlign = TextAlign.Start)
                content()
            }
        }
    }
}
