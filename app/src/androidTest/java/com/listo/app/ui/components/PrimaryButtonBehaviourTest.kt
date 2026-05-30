package com.listo.app.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PrimaryButtonBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun click_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            PrimaryButton(text = "Save", onClick = { clicked = true })
        }
        composeRule.onNodeWithText("Save").performClick()
        assertTrue(clicked)
    }

    @Test
    fun disabledButton_doesNotInvokeCallback() {
        var clicked = false
        composeRule.setContent {
            PrimaryButton(text = "Save", onClick = { clicked = true }, enabled = false)
        }
        composeRule.onNodeWithText("Save").performClick()
        assertFalse(clicked)
    }
}
