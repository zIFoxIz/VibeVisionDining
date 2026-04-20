package com.example.vibevision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.vibevision.di.AppContainer
import com.example.vibevision.ml.PredictionResult
import com.example.vibevision.ml.ReviewSentimentPredictor
import com.google.gson.JsonParser
import com.google.firebase.FirebaseApp
import com.example.vibevision.ml.SentimentAnalyzer
import com.example.vibevision.ml.TFLiteSentimentAnalyzer
import com.example.vibevision.ui.app.VibeVisionEntryFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        FirebaseApp.initializeApp(applicationContext)

        // Load model JSON from assets
        val modelAnalyzer: ReviewSentimentPredictor = try {
            // Prefer TFLite model when present, then fallback to JSON model.
            val tfliteBytes = assets.open("sentiment_model.tflite").readBytes()
            val tfliteMeta = assets.open("sentiment_model_tflite_metadata.json").bufferedReader().use {
                JsonParser.parseString(it.readText()).asJsonObject
            }

            val classes = tfliteMeta.getAsJsonArray("classes").map { it.asString }
            val topFeatures = tfliteMeta.getAsJsonArray("top_features").map { it.asString }
            val stopwords = SentimentAnalyzer.getEnglishStopwords()

            TFLiteSentimentAnalyzer(
                modelBytes = tfliteBytes,
                classes = classes,
                topFeatures = topFeatures,
                englishStopwords = stopwords
            )
        } catch (_: Exception) {
            try {
            val modelJson = assets.open("sentiment_model_minimal.json").bufferedReader().use {
                JsonParser.parseString(it.readText()).asJsonObject
            }
            val stopwords = SentimentAnalyzer.getEnglishStopwords()
            SentimentAnalyzer(modelJson, stopwords)
            } catch (_: Exception) {
                // Last-resort fallback to keep app functional in development.
                object : ReviewSentimentPredictor {
                    override fun predict(text: String): PredictionResult {
                        return PredictionResult(
                            sentiment = "neutral",
                            label = 1,
                            confidence = 0.33f,
                            scores = mapOf("negative" to 0.33f, "neutral" to 0.33f, "positive" to 0.33f)
                        )
                    }
                }
            }
        }

        setContent {
            VibeVisionEntryFlow(analyzer = modelAnalyzer)
        }
    }
}
