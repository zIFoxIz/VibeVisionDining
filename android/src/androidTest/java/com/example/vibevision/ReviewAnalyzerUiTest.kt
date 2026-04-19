package com.example.vibevision

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasSetTextAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ReviewAnalyzerUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun reviewAnalyzer_showsValidationErrorForBlankInput() {
        composeRule.onNodeWithText("Analyze").performClick()
        composeRule.onNodeWithText("Analyze Sentiment").performClick()

        composeRule.onNodeWithText("Please enter a review text").assertIsDisplayed()
    }

    @Test
    fun reviewAnalyzer_displaysResultForValidReview() {
        composeRule.onNodeWithText("Analyze").performClick()

        composeRule.onNode(hasSetTextAction())
            .performTextInput("Great food and friendly service")

        composeRule.onNodeWithText("Analyze Sentiment").performClick()

        composeRule.onNodeWithText("Analysis Result").assertIsDisplayed()
        composeRule.onNodeWithText("Score Breakdown:").assertIsDisplayed()
    }
}
