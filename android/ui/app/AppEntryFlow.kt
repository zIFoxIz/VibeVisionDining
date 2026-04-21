package com.example.vibevision.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibevision.BuildConfig
import com.example.vibevision.ml.ReviewSentimentPredictor
import com.example.vibevision.ui.theme.InkBlue
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.VibeVisionTheme
import com.example.vibevision.ui.theme.WarmOrange
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private enum class EntryStage {
    WELCOME,
    AUTH,
    APP
}

private enum class AuthMode {
    LOGIN,
    CREATE
}

@Composable
fun VibeVisionEntryFlow(analyzer: ReviewSentimentPredictor) {
    val context = LocalContext.current
    val firebaseConfigured = remember {
        runCatching {
            FirebaseApp.getApps(context).isNotEmpty() ||
                FirebaseApp.initializeApp(context) != null ||
                initializeFirebaseFromBuildConfig(context)
        }
            .getOrDefault(false)
    }
    val auth = remember(firebaseConfigured) { if (firebaseConfigured) FirebaseAuth.getInstance() else null }
    val scope = rememberCoroutineScope()
    var stage by rememberSaveable { mutableStateOf(EntryStage.WELCOME) }
    var authLoading by rememberSaveable { mutableStateOf(false) }
    var authError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(stage) {
        if (stage == EntryStage.WELCOME) {
            delay(5000)
            stage = if (auth?.currentUser != null) EntryStage.APP else EntryStage.AUTH
        }
    }

    DisposableEffect(auth) {
        if (auth == null) {
            onDispose { }
        } else {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (stage != EntryStage.WELCOME) {
                    stage = if (firebaseAuth.currentUser != null) EntryStage.APP else EntryStage.AUTH
                }
            }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        }
    }

    AnimatedVisibility(
        visible = stage == EntryStage.WELCOME,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        WelcomeScreen(onContinue = { stage = EntryStage.AUTH })
    }

    AnimatedVisibility(
        visible = stage == EntryStage.AUTH,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AuthScreen(
            isLoading = authLoading,
            errorMessage = authError,
            onDismissError = { authError = null },
            onContinueAsGuest = {
                val authRef = auth
                if (authRef == null) {
                    authError = "Firebase is not configured. Add google-services.json or FIREBASE_* values in local.properties."
                    return@AuthScreen
                }
                authLoading = true
                scope.launch {
                    performFirebaseAuth(
                        action = { authRef.signInAnonymously().await() },
                        onError = { authError = it },
                        onComplete = { authLoading = false }
                    )
                }
            },
            onAuthSuccess = { mode, email, password ->
                val authRef = auth
                if (authRef == null) {
                    authError = "Firebase is not configured. Add google-services.json or FIREBASE_* values in local.properties."
                    return@AuthScreen
                }

                authLoading = true
                scope.launch {
                    val action: suspend () -> Unit = {
                        if (mode == AuthMode.LOGIN) {
                            authRef.signInWithEmailAndPassword(email, password).await()
                        } else {
                            authRef.createUserWithEmailAndPassword(email, password).await()
                        }
                    }

                    performFirebaseAuth(
                        action = action,
                        onError = { authError = it },
                        onComplete = { authLoading = false }
                    )
                }
            }
        )
    }

    AnimatedVisibility(
        visible = stage == EntryStage.APP,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        VibeVisionApp(analyzer = analyzer)
    }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    VibeVisionTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF5E9),
                            Color(0xFFF7E5CD),
                            Color(0xFFE8F2EE)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Rose.copy(alpha = 0.15f))
                    .align(Alignment.TopEnd)
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(SageGreen.copy(alpha = 0.18f))
                    .align(Alignment.BottomStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 30.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = "Restaurant",
                                tint = WarmOrange,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "VibeVision Dining",
                                style = MaterialTheme.typography.headlineSmall,
                                color = InkBlue
                            )
                        }

                        Text(
                            text = "Find your food mood.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlue
                        )

                        Text(
                            text = "Discover restaurants, map your vibe, and track sentiment in one bold experience.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkBlue.copy(alpha = 0.86f)
                        )
                    }
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = "Continue", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onAuthSuccess: (AuthMode, String, String) -> Unit
) {
    VibeVisionTheme(darkTheme = false) {
        var mode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var confirmPassword by rememberSaveable { mutableStateOf("") }
        var showPassword by rememberSaveable { mutableStateOf(false) }
        val canSubmit = email.isNotBlank() && password.length >= 6 &&
            (mode == AuthMode.LOGIN || confirmPassword == password)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF7EA),
                            Color(0xFFF8E8D3),
                            Color(0xFFE7F2EE)
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(SageGreen.copy(alpha = 0.14f))
                    .align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(WarmOrange.copy(alpha = 0.16f))
                    .align(Alignment.TopEnd)
            )

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Rose.copy(alpha = 0.12f))
                    .align(Alignment.BottomCenter)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Welcome to VibeVision Dining",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkBlue
                    )

                    Text(
                        text = "Sign in to sync preferences and discover your next spot.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkBlue.copy(alpha = 0.8f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { mode = AuthMode.LOGIN },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mode == AuthMode.LOGIN) SageGreen else Color(0xFFE7EFEA),
                                contentColor = if (mode == AuthMode.LOGIN) Color.White else InkBlue
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Log In")
                        }

                        Button(
                            onClick = { mode = AuthMode.CREATE },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mode == AuthMode.CREATE) WarmOrange else Color(0xFFF5ECE3),
                                contentColor = if (mode == AuthMode.CREATE) Color.White else InkBlue
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Create Account")
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            focusedLabelColor = SageGreen
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            focusedLabelColor = SageGreen
                        )
                    )

                    if (mode == AuthMode.CREATE) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirm password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WarmOrange,
                                focusedLabelColor = WarmOrange
                            )
                        )
                    }

                    Button(
                        onClick = { onAuthSuccess(mode, email.trim(), password) },
                        enabled = canSubmit && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlue)
                    ) {
                        Text(if (isLoading) "Please wait..." else if (mode == AuthMode.LOGIN) "Log In" else "Create Account")
                    }

                    Button(
                        onClick = onContinueAsGuest,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8F0ED),
                            contentColor = InkBlue
                        )
                    ) {
                        Text("Continue as Guest")
                    }

                    if (errorMessage != null) {
                        Text(text = errorMessage, color = Rose)
                        Button(onClick = onDismissError, modifier = Modifier.fillMaxWidth()) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun performFirebaseAuth(
    action: suspend () -> Unit,
    onError: (String) -> Unit,
    onComplete: () -> Unit
) {
    runCatching { action() }
        .onFailure { error ->
            onError(toFriendlyAuthError(error))
        }
    onComplete()
}

private fun toFriendlyAuthError(error: Throwable): String {
    val raw = error.message.orEmpty()
    return when {
        raw.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
            "Firebase Authentication is not fully enabled for this project. In Firebase Console, open Authentication and enable Email/Password (and Google if needed)."
        raw.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
            "This sign-in method is disabled. Enable it in Firebase Console > Authentication > Sign-in method."
        raw.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
            "Invalid email or password."
        raw.contains("EMAIL_EXISTS", ignoreCase = true) ->
            "This email already has an account. Try Log In instead."
        raw.contains("network", ignoreCase = true) ->
            "Network issue. Check connection and try again."
        raw.isNotBlank() -> raw
        else -> "Authentication failed."
    }
}

private fun initializeFirebaseFromBuildConfig(context: android.content.Context): Boolean {
    if (BuildConfig.FIREBASE_APP_ID.isBlank() ||
        BuildConfig.FIREBASE_API_KEY.isBlank() ||
        BuildConfig.FIREBASE_PROJECT_ID.isBlank()
    ) {
        return false
    }

    if (FirebaseApp.getApps(context).isNotEmpty()) return true

    val optionsBuilder = FirebaseOptions.Builder()
        .setApplicationId(BuildConfig.FIREBASE_APP_ID)
        .setApiKey(BuildConfig.FIREBASE_API_KEY)
        .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)

    if (BuildConfig.FIREBASE_STORAGE_BUCKET.isNotBlank()) {
        optionsBuilder.setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
    }
    if (BuildConfig.FIREBASE_GCM_SENDER_ID.isNotBlank()) {
        optionsBuilder.setGcmSenderId(BuildConfig.FIREBASE_GCM_SENDER_ID)
    }

    return runCatching {
        FirebaseApp.initializeApp(context, optionsBuilder.build()) != null
    }.getOrDefault(false)
}
