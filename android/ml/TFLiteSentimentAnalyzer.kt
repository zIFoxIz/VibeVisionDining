package com.example.vibevision.ml

import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteSentimentAnalyzer(
    modelBytes: ByteArray,
    private val classes: List<String>,
    private val topFeatures: List<String>,
    private val englishStopwords: Set<String>
) : ReviewSentimentPredictor {

    private val interpreter: Interpreter = Interpreter(modelBytes)

    private fun preprocessText(text: String): List<String> {
        return text
            .lowercase()
            .split(Regex("[\\s\\p{P}]+"))
            .filter { token -> token.length > 1 && token !in englishStopwords }
    }

    private fun extractFeatures(tokens: List<String>): FloatArray {
        val features = FloatArray(topFeatures.size)
        for (i in topFeatures.indices) {
            features[i] = tokens.count { it == topFeatures[i] }.toFloat()
        }
        return features
    }

    override fun predict(text: String): PredictionResult {
        val tokens = preprocessText(text)
        if (tokens.isEmpty()) {
            return PredictionResult(
                sentiment = "neutral",
                label = 1,
                confidence = 0.33f,
                scores = mapOf("negative" to 0.33f, "neutral" to 0.33f, "positive" to 0.33f)
            )
        }

        val features = extractFeatures(tokens)
        val inputBuffer = ByteBuffer.allocateDirect(features.size * 4).order(ByteOrder.nativeOrder())
        features.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()

        val output = Array(1) { FloatArray(classes.size) }
        interpreter.run(inputBuffer, output)

        val probabilities = output[0]
        val predictedLabel = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val predictedSentiment = classes[predictedLabel]
        val confidence = probabilities[predictedLabel]

        return PredictionResult(
            sentiment = predictedSentiment,
            label = predictedLabel,
            confidence = confidence,
            scores = classes.indices.associate { i -> classes[i] to probabilities[i] }
        )
    }
}
