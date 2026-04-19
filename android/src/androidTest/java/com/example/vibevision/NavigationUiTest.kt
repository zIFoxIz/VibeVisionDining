package com.example.vibevision

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class NavigationUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigation_navigatesAcrossPrimaryTabs() {
        composeRule.onNodeWithText("Home Feed").assertIsDisplayed()

        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Restaurant Search").assertIsDisplayed()

        composeRule.onNodeWithText("Analyze").performClick()
        composeRule.onNodeWithText("Restaurant Review Sentiment Analysis").assertIsDisplayed()

        composeRule.onNodeWithText("Insights").performClick()
        composeRule.onNodeWithText("Advanced Analytics Dashboard").assertIsDisplayed()

        composeRule.onNodeWithText("Profile").performClick()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }
}
