package com.example.vibevision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibevision.ml.PredictionResult

@Composable
fun SentimentAnalysisScreen(viewModel: SentimentViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "VibeVision Dining",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Text(
                text = "Restaurant Review Sentiment Analysis",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Text Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paste or Enter Review",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.reviewText,
                    onValueChange = { viewModel.updateReviewText(it) },
                    placeholder = { Text("Enter restaurant review here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 6,
                    minLines = 4
                )
            }
        }

        // Analyze Button
        Button(
            onClick = { viewModel.analyzeReview() },
            enabled = !uiState.isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1565C0),
                disabledContainerColor = Color.LightGray
            )
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Analyze Sentiment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Error Message
        uiState.error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = it,
                    color = Color(0xFFC62828),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Results
        uiState.sentimentResult?.let { result ->
            ResultCard(result, uiState.sentimentEmoji)
            SentimentChartCard(result)
        }
    }
}

@Composable
fun ResultCard(result: PredictionResult, emoji: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Analysis Result",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Sentiment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sentiment:", fontWeight = FontWeight.SemiBold)
                if (emoji != null) {
                    Text(text = emoji, fontSize = 24.sp)
                }
                
                val backgroundColor = when (result.sentiment) {
                    "positive" -> Color(0xFF4CAF50)
                    "negative" -> Color(0xFFF44336)
                    else -> Color(0xFFFFC107)
                }
                
                Surface(
                    color = backgroundColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = result.sentiment.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Confidence:", fontWeight = FontWeight.SemiBold)
                Text(
                    text = String.format("%.1f%%", result.confidence * 100),
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score Breakdown
            Text(
                text = "Score Breakdown:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            result.scores.forEach { (label, score) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format("%.2f", score),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SentimentChartCard(result: PredictionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sentiment Distribution",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Simple Bar Chart
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                result.scores.forEach { (label, score) ->
                    BarChartRow(label, score)
                }
            }
        }
    }
}

@Composable
fun BarChartRow(label: String, value: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Text(text = String.format("%.1f%%", value * 100), fontSize = 12.sp, color = Color.Gray)
        }

        // Bar
        val barColor = when (label) {
            "positive" -> Color(0xFF4CAF50)
            "negative" -> Color(0xFFF44336)
            else -> Color(0xFFFFC107)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(value.coerceIn(0f, 1f))
                .height(24.dp)
                .background(barColor, shape = MaterialTheme.shapes.small)
        )
    }
}
