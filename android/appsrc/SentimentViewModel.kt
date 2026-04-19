package com.example.vibevision.ui

import androidx.lifecycle.ViewModel
import com.example.vibevision.domain.EmojiSentimentMapper
import com.example.vibevision.ml.ReviewSentimentPredictor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SentimentState(
    val reviewText: String = "",
    val sentimentResult: com.example.vibevision.ml.PredictionResult? = null,
    val sentimentEmoji: String? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null
)

class SentimentViewModel(private val analyzer: ReviewSentimentPredictor) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SentimentState())
    val uiState: StateFlow<SentimentState> = _uiState.asStateFlow()

    fun updateReviewText(text: String) {
        _uiState.value = _uiState.value.copy(reviewText = text)
    }

    fun analyzeReview() {
        if (_uiState.value.reviewText.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a review text",
                sentimentResult = null
            )
            return
        }

        try {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null)
            
            val result = analyzer.predict(_uiState.value.reviewText)
            val emoji = EmojiSentimentMapper.emojiFor(result.sentiment, result.confidence)
            
            _uiState.value = _uiState.value.copy(
                sentimentResult = result,
                sentimentEmoji = emoji,
                isAnalyzing = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Error analyzing review: ${e.message}",
                isAnalyzing = false
            )
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(sentimentResult = null, sentimentEmoji = null, error = null)
    }
}
