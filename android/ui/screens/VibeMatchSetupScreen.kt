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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.VibePreference

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
            Text(text = "Vibe Match Setup", fontWeight = FontWeight.Bold)
            Text(text = "Enable the vibes you want the recommendation engine to prioritize.")
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Preference Coverage", fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                    Text(text = "$enabled of ${preferences.size} vibes enabled")
                }
            }
        }

        items(preferences) { pref ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = pref.vibe)
                    Switch(checked = pref.enabled, onCheckedChange = { onToggle(pref.vibe) })
                }
            }
        }
    }
}
