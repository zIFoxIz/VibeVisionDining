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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibevision.ui.components.SectionHeader

@Composable
fun AccountSettingsScreen(
    email: String,
    accountActionMessage: String?,
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit,
    onDismissMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Account Settings", subtitle = "Manage login and security options")

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Signed in email", fontWeight = FontWeight.SemiBold)
                Text(text = email.ifBlank { "Not provided" })

                Button(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
                    Text("Change Password")
                }

                Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign Out")
                }
            }
        }

        if (accountActionMessage != null) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = accountActionMessage)
                    Button(onClick = onDismissMessage, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}
