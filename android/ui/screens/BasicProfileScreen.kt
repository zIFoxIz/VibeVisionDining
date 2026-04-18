package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.LanguageOption
import com.example.vibevision.model.VibePreference

@Composable
fun BasicProfileScreen(
    preferences: List<VibePreference>,
    isDarkMode: Boolean,
    isOfflineMode: Boolean,
    pushNotificationsEnabled: Boolean,
    language: LanguageOption,
    onToggle: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onOfflineModeToggle: (Boolean) -> Unit,
    onPushNotificationsToggle: (Boolean) -> Unit,
    onLanguageChange: (LanguageOption) -> Unit
) {
    var languageExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Basic Profile", fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "User Vibe Preferences Setup", fontWeight = FontWeight.SemiBold)
                preferences.forEach { pref ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = pref.vibe)
                        Switch(checked = pref.enabled, onCheckedChange = { onToggle(pref.vibe) })
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "User Account Settings", fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Display Name")
                    Text(text = "VibeExplorer")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Email")
                    Text(text = "user@vibevision.app")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Light Mode and Dark Mode")
                    Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Offline Mode")
                    Switch(checked = isOfflineMode, onCheckedChange = onOfflineModeToggle)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Push Notifications")
                    Switch(checked = pushNotificationsEnabled, onCheckedChange = onPushNotificationsToggle)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Multi-Language Support")
                    Text(text = language.name)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Select Language")
                    Text(text = "Change")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "")
                    androidx.compose.material3.Button(onClick = { languageExpanded = true }) {
                        Text("Open")
                    }
                }

                DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    LanguageOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                onLanguageChange(option)
                                languageExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
