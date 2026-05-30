package com.listo.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class EmptyStateBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTitleAndSubtitle() {
        composeRule.setContent {
            EmptyState(title = "No lists", subtitle = "Tap to add one")
        }
        composeRule.onNodeWithText("No lists").assertIsDisplayed()
        composeRule.onNodeWithText("Tap to add one").assertIsDisplayed()
    }
}
