package com.example.vibevision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.gson.JsonParser
import com.example.vibevision.ml.SentimentAnalyzer
import com.example.vibevision.ui.app.VibeVisionApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load model JSON from assets
        val modelAnalyzer = try {
            val modelJson = assets.open("sentiment_model_minimal.json").bufferedReader().use {
                JsonParser.parseString(it.readText()).asJsonObject
            }
            val stopwords = SentimentAnalyzer.getEnglishStopwords()
            SentimentAnalyzer(modelJson, stopwords)
        } catch (e: Exception) {
            // Fallback: create dummy analyzer (for testing without model file)
            val dummyJson = com.google.gson.JsonObject().apply {
                add("classes", com.google.gson.JsonArray().apply {
                    add("negative")
                    add("neutral")
                    add("positive")
                })
                add("coef", com.google.gson.JsonArray())
                add("intercept", com.google.gson.JsonArray())
                add("top_features", com.google.gson.JsonArray())
            }
            SentimentAnalyzer(dummyJson, SentimentAnalyzer.getEnglishStopwords())
        }

        setContent {
            VibeVisionApp(analyzer = modelAnalyzer)
        }
    }
}
