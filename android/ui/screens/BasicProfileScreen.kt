package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.LanguageOption
import androidx.compose.foundation.text.KeyboardOptions
import com.example.vibevision.model.VibePreference
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.components.ToggleRow

@Composable
fun BasicProfileScreen(
    preferences: List<VibePreference>,
    isDarkMode: Boolean,
    isOfflineMode: Boolean,
    pushNotificationsEnabled: Boolean,
    language: LanguageOption,
    profileName: String,
    profileAddress: String,
    profilePhone: String,
    profileEmail: String,
    profileSavedMessage: String?,
    onToggle: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onOfflineModeToggle: (Boolean) -> Unit,
    onPushNotificationsToggle: (Boolean) -> Unit,
    onLanguageChange: (LanguageOption) -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onDismissSavedMessage: () -> Unit,
    onOpenVibeSetup: () -> Unit
) {
    var languageExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Profile", subtitle = "Personalize your dining intelligence experience")

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "User Vibe Preferences Setup", fontWeight = FontWeight.SemiBold)
                preferences.forEach { pref ->
                    ToggleRow(label = pref.vibe, checked = pref.enabled) { onToggle(pref.vibe) }
                }
                Button(onClick = onOpenVibeSetup, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Vibe Match Setup")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "User Account Settings", fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = profileName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Full Name") }
                )

                OutlinedTextField(
                    value = profileAddress,
                    onValueChange = onAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Address") }
                )

                OutlinedTextField(
                    value = profilePhone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = profileEmail,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Button(onClick = onSaveProfile, modifier = Modifier.fillMaxWidth()) {
                    Text("Save Profile")
                }

                if (profileSavedMessage != null) {
                    Text(text = profileSavedMessage)
                    Button(onClick = onDismissSavedMessage, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }

                ToggleRow(label = "Light Mode and Dark Mode", checked = isDarkMode, onChange = onDarkModeToggle)
                ToggleRow(label = "Offline Mode", checked = isOfflineMode, onChange = onOfflineModeToggle)
                ToggleRow(label = "Push Notifications", checked = pushNotificationsEnabled, onChange = onPushNotificationsToggle)

                Text(text = "Multi-Language Support: ${language.name}")

                Button(onClick = { languageExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Select Language")
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
