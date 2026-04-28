package com.example.vibevision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vibevision.model.LanguageOption
import com.example.vibevision.ui.components.SectionHeader
import com.example.vibevision.ui.components.ToggleRow
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen

@Composable
fun AccountSettingsScreen(
    isDarkMode: Boolean,
    isOfflineMode: Boolean,
    pushNotificationsEnabled: Boolean,
    language: LanguageOption,
    profileName: String,
    profileDob: String,
    profileAddress: String,
    profilePhone: String,
    email: String,
    profileSavedMessage: String?,
    accountActionMessage: String?,
    onDarkModeToggle: (Boolean) -> Unit,
    onOfflineModeToggle: (Boolean) -> Unit,
    onPushNotificationsToggle: (Boolean) -> Unit,
    onLanguageChange: (LanguageOption) -> Unit,
    onNameChange: (String) -> Unit,
    onDobChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onDismissSavedMessage: () -> Unit,
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit,
    onDismissMessage: () -> Unit
) {
    var languageExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Account Settings", subtitle = "Manage profile, app behavior, and security")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Profile Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = profileName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Full Name") },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = profileDob,
                    onValueChange = onDobChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Date of Birth") },
                    placeholder = { Text("MM/DD/YYYY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = profileAddress,
                    onValueChange = onAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Address") },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = profilePhone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = onSaveProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Profile", style = MaterialTheme.typography.labelLarge)
                }

                if (profileSavedMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = profileSavedMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = SageGreen
                            )
                        }
                        TextButton(onClick = onDismissSavedMessage) {
                            Text("Dismiss", color = SageGreen)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "App Preferences", style = MaterialTheme.typography.titleMedium)

                ToggleRow(label = "Dark Mode", checked = isDarkMode, onChange = onDarkModeToggle)
                ToggleRow(label = "Offline Mode", checked = isOfflineMode, onChange = onOfflineModeToggle)
                ToggleRow(label = "Push Notifications", checked = pushNotificationsEnabled, onChange = onPushNotificationsToggle)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Language: ${language.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    onClick = { languageExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Change Language", style = MaterialTheme.typography.labelLarge)
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Account & Security", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = email.ifBlank { "Not provided" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                OutlinedButton(
                    onClick = onChangePassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Change Password", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Sign Out", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (accountActionMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = accountActionMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = SageGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismissMessage) {
                        Text("Dismiss", color = SageGreen)
                    }
                }
            }
        }
    }
}
