package com.example.vibevision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.components.SectionHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BasicProfileScreen(
    preferences: List<VibePreference>,
    profileName: String,
    profileEmail: String,
    favoriteCount: Int,
    recommendationCount: Int,
    onOpenVibeSetup: () -> Unit
) {
    val enabledVibes = preferences.filter { it.enabled }.map { it.vibe }
    val firstName = profileName.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
    val displayName = if (firstName.isNotBlank()) firstName else "Food Explorer"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Profile", subtitle = "Your dining identity and vibe DNA")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1D3557), Color(0xFF2A9D8F))
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Taste Passport", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Welcome back, $displayName",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (profileEmail.isBlank()) "No email saved" else profileEmail,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Vibe DNA", fontWeight = FontWeight.SemiBold)
                if (enabledVibes.isEmpty()) {
                    Text("No vibes selected yet. Build your dining personality in Vibe Match Setup.")
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        enabledVibes.forEach { vibe ->
                            Surface(
                                color = Color(0xFFEAF4EF),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = vibe,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color(0xFF1F5135),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
                Button(onClick = onOpenVibeSetup, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Vibe Match Setup")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Dining Pulse", fontWeight = FontWeight.SemiBold)
                PulseBadge(label = "Saved Favorites", value = favoriteCount.toString(), modifier = Modifier.fillMaxWidth())
                PulseBadge(label = "AI Picks Ready", value = recommendationCount.toString(), modifier = Modifier.fillMaxWidth())
                Text(text = "Tip: Use Account Settings to manage profile details, appearance, and app behavior.")
            }
        }
    }
}

@Composable
private fun PulseBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF4EFE5))
            .padding(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
