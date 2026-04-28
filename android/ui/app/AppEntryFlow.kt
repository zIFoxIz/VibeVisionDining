package com.example.vibevision.ui.app

import android.content.Context
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
import com.example.vibevision.ui.components.BrandLogoMark
import com.example.vibevision.ui.theme.InkBlue
import com.example.vibevision.ui.theme.Rose
import com.example.vibevision.ui.theme.SageGreen
import com.example.vibevision.ui.theme.VibeVisionTheme
import com.example.vibevision.ui.theme.WarmOrange
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TUTORIAL_PREFS_NAME = "vibevision_tutorial"
private const val TUTORIAL_KEY_PREFIX = "completed_"

private enum class EntryStage {
    WELCOME,
    AUTH,
    TUTORIAL,
    APP
}

private enum class AuthMode {
    LOGIN,
    CREATE
}

@Composable
fun VibeVisionEntryFlow(analyzer: ReviewSentimentPredictor) {
    val context = LocalContext.current
    val onboardingPrefs = remember(context) {
        context.getSharedPreferences(TUTORIAL_PREFS_NAME, Context.MODE_PRIVATE)
    }
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
    var openVibeSetupOnLaunch by rememberSaveable { mutableStateOf(false) }
    var authLoading by rememberSaveable { mutableStateOf(false) }
    var authError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(stage) {
        if (stage == EntryStage.WELCOME) {
            delay(5000)
            val currentUser = auth?.currentUser
            if (currentUser != null) {
                val completedTutorial = hasCompletedTutorial(onboardingPrefs, currentUser)
                openVibeSetupOnLaunch = false
                stage = if (completedTutorial) EntryStage.APP else EntryStage.TUTORIAL
            } else {
                stage = EntryStage.AUTH
            }
        }
    }

    DisposableEffect(auth, stage, onboardingPrefs) {
        if (auth == null) {
            onDispose { }
        } else {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser == null) {
                    openVibeSetupOnLaunch = false
                    if (stage != EntryStage.WELCOME) {
                        stage = EntryStage.AUTH
                    }
                } else if (stage == EntryStage.AUTH || stage == EntryStage.APP) {
                    val completedTutorial = hasCompletedTutorial(onboardingPrefs, firebaseAuth.currentUser)
                    openVibeSetupOnLaunch = false
                    stage = if (completedTutorial) EntryStage.APP else EntryStage.TUTORIAL
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
                authError = null
                scope.launch {
                    performFirebaseAuth(
                        action = { authRef.signInAnonymously().await() },
                        onSuccess = {
                            val completedTutorial = hasCompletedTutorial(onboardingPrefs, authRef.currentUser)
                            if (completedTutorial) {
                                openVibeSetupOnLaunch = false
                                stage = EntryStage.APP
                            } else {
                                openVibeSetupOnLaunch = true
                                stage = EntryStage.TUTORIAL
                            }
                        },
                        onError = { authError = it },
                        onComplete = { authLoading = false }
                    )
                }
            },
            onForgotPassword = { email ->
                val authRef = auth
                if (authRef == null) {
                    authError = "Firebase is not configured. Add google-services.json or FIREBASE_* values in local.properties."
                    return@AuthScreen
                }

                val normalizedEmail = email.trim()
                if (normalizedEmail.isBlank()) {
                    authError = "Enter your email first, then tap Forgot Password."
                    return@AuthScreen
                }

                authLoading = true
                authError = null
                scope.launch {
                    runCatching {
                        authRef.sendPasswordResetEmail(normalizedEmail).await()
                    }.onSuccess {
                        authError = "Password reset email sent to $normalizedEmail."
                    }.onFailure { error ->
                        authError = toFriendlyAuthError(error)
                    }
                    authLoading = false
                }
            },
            onAuthSuccess = { mode, email, password ->
                val authRef = auth
                if (authRef == null) {
                    authError = "Firebase is not configured. Add google-services.json or FIREBASE_* values in local.properties."
                    return@AuthScreen
                }

                authLoading = true
                authError = null
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
                        onSuccess = {
                            val completedTutorial = hasCompletedTutorial(onboardingPrefs, authRef.currentUser)
                            if (completedTutorial) {
                                openVibeSetupOnLaunch = false
                                stage = EntryStage.APP
                            } else {
                                openVibeSetupOnLaunch = mode == AuthMode.CREATE
                                stage = EntryStage.TUTORIAL
                            }
                        },
                        onError = { authError = it },
                        onComplete = { authLoading = false },
                        alwaysUseSignInError = mode == AuthMode.LOGIN
                    )
                }
            }
        )
    }

    AnimatedVisibility(
        visible = stage == EntryStage.TUTORIAL,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PostAuthTutorialScreen(
            onSetUpVibes = {
                setTutorialCompleted(onboardingPrefs, auth?.currentUser)
                openVibeSetupOnLaunch = true
                stage = EntryStage.APP
            },
            onSkip = {
                setTutorialCompleted(onboardingPrefs, auth?.currentUser)
                openVibeSetupOnLaunch = false
                stage = EntryStage.APP
            }
        )
    }

    AnimatedVisibility(
        visible = stage == EntryStage.APP,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        VibeVisionApp(
            analyzer = analyzer,
            startInVibeSetup = openVibeSetupOnLaunch,
            onStartDestinationConsumed = { openVibeSetupOnLaunch = false }
        )
    }
}

@Composable
private fun PostAuthTutorialScreen(
    onSetUpVibes: () -> Unit,
    onSkip: () -> Unit
) {
    VibeVisionTheme(darkTheme = false) {
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
                        text = "How VibeVision Works",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. Set your vibe preferences first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkBlue.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "2. Search restaurants by city, name, or address.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkBlue.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "3. Use Insights and sentiment summaries to pick your best match.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkBlue.copy(alpha = 0.9f)
                    )

                    Button(
                        onClick = onSetUpVibes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlue)
                    ) {
                        Text("Set Up My Vibes")
                    }

                    Button(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8F0ED),
                            contentColor = InkBlue
                        )
                    ) {
                        Text("Skip for Now")
                    }
                }
            }
        }
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BrandLogoMark()

                        Text(
                            text = "Find your food mood",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlue
                        )

                        Text(
                            text = "Get AI restaurant recommendations that match your vibe and what you are craving.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkBlue.copy(alpha = 0.86f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7F3))
                            ) {
                                Text(
                                    text = "AI picks",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SageGreen
                                )
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Text(
                                    text = "Vibe match",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = WarmOrange
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = InkBlue.copy(alpha = 0.06f))
                        ) {
                            Text(
                                text = "AI recommendations tailored to your vibe, taste, and dining style.",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkBlue
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your next meal, matched to your mood.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkBlue.copy(alpha = 0.75f)
                    )
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
}

@Composable
private fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onForgotPassword: (String) -> Unit,
    onAuthSuccess: (AuthMode, String, String) -> Unit
) {
    VibeVisionTheme(darkTheme = false) {
        var mode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var confirmPassword by rememberSaveable { mutableStateOf("") }
        var showPassword by rememberSaveable { mutableStateOf(false) }
        val canSubmit = email.trim().isNotBlank() && password.length >= 6 &&
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

                    if (mode == AuthMode.LOGIN) {
                        Button(
                            onClick = { onForgotPassword(email) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF2F6F4),
                                contentColor = InkBlue
                            )
                        ) {
                            Text("Forgot Password")
                        }
                    }

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
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onComplete: () -> Unit,
    alwaysUseSignInError: Boolean = false
) {
    runCatching { action() }
        .onSuccess { onSuccess() }
        .onFailure { error ->
            val friendlyMessage = toFriendlyAuthError(error)
            val credentialFailure = friendlyMessage.equals("Invalid email or password.", ignoreCase = true) ||
                friendlyMessage.equals("Incorrect password. Tap Forgot Password to reset it.", ignoreCase = true) ||
                friendlyMessage.equals("No account found for that email. Use Create Account first.", ignoreCase = true)

            val message = if (alwaysUseSignInError && credentialFailure) {
                "Email or password is incorrect."
            } else {
                friendlyMessage
            }
            onError(message)
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
        raw.contains("INVALID_PASSWORD", ignoreCase = true) ->
            "Incorrect password. Tap Forgot Password to reset it."
        raw.contains("EMAIL_NOT_FOUND", ignoreCase = true) ->
            "No account found for that email. Use Create Account first."
        raw.contains("INVALID_EMAIL", ignoreCase = true) ->
            "Email address format is invalid."
        raw.contains("USER_DISABLED", ignoreCase = true) ->
            "This account is disabled in Firebase Authentication."
        raw.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) ->
            "Too many attempts. Wait a bit, then try again or reset your password."
        raw.contains("EMAIL_EXISTS", ignoreCase = true) ->
            "This email already has an account. Try Log In instead."
        raw.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) ->
            "This email already has an account. Try Log In instead."
        raw.contains("network", ignoreCase = true) ->
            "Network issue. Check connection and try again."
        raw.isNotBlank() -> raw
        else -> "Authentication failed."
    }
}

private fun tutorialIdentity(user: FirebaseUser?): String {
    return if (user == null || user.isAnonymous) {
        "guest_device"
    } else {
        "user_${user.uid}"
    }
}

private fun tutorialCompletionKey(user: FirebaseUser?): String =
    "$TUTORIAL_KEY_PREFIX${tutorialIdentity(user)}"

private fun hasCompletedTutorial(prefs: android.content.SharedPreferences, user: FirebaseUser?): Boolean {
    return prefs.getBoolean(tutorialCompletionKey(user), false)
}

private fun setTutorialCompleted(prefs: android.content.SharedPreferences, user: FirebaseUser?) {
    prefs.edit().putBoolean(tutorialCompletionKey(user), true).apply()
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
