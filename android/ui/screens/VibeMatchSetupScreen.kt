package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.theme.InkBlue
import com.example.vibevision.ui.theme.NeutralGray
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.WarmOrange

@Composable
fun VibeMatchSetupScreen(
    preferences: List<VibePreference>,
    onToggle: (String) -> Unit
) {
    val enabled = preferences.count { it.enabled }
    val progress = if (preferences.isEmpty()) 0f else enabled.toFloat() / preferences.size.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Vibe Match Setup",
                subtitle = "Enable the vibes you want the recommendation engine to prioritize"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = SageGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Preference Coverage",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = InkBlue
                        )
                    }
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = SageGreen,
                        trackColor = SageGreen.copy(alpha = 0.18f)
                    )
                    Text(
                        text = "$enabled of ${preferences.size} vibes enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        items(preferences) { pref ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pref.enabled) SageGreen.copy(alpha = 0.06f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = pref.vibe,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (pref.enabled) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (pref.enabled) InkBlue else NeutralGray
                        )
                        Text(
                            text = if (pref.enabled) "Active — will influence recommendations" else "Inactive",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (pref.enabled) SageGreen else NeutralGray
                        )
                    }
                    Switch(
                        checked = pref.enabled,
                        onCheckedChange = { onToggle(pref.vibe) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SageGreen,
                            checkedTrackColor = SageGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = NeutralGray,
                            uncheckedTrackColor = NeutralGray.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}
