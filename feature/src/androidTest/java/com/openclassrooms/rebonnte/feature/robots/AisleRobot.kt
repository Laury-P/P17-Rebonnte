package com.openclassrooms.rebonnte.feature.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.openclassrooms.rebonnte.feature.R

class AisleRobot(private val composeTestRule: AndroidComposeTestRule<*, *>) {

    private val context = composeTestRule.activity

    fun clickAddAisle() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.aisle_add_description)).performClick()
    }

    fun enterAisleName(name: String) {
        composeTestRule.onNodeWithText(context.getString(R.string.aisle_new_label)).performTextInput(name)
    }

    fun clickSaveAisle() {
        composeTestRule.onNodeWithText(context.getString(R.string.aisle_save)).performClick()
    }

    fun clickAisle(name: String) {
        composeTestRule.onNodeWithText(name).performClick()
    }

    fun clickLogout() {
        val description = context.getString(R.string.aisle_logout_description)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription(description)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription(description).performClick()
    }

    fun clickRetry() {
        composeTestRule.onNodeWithText("Retry").performClick()
    }

    infix fun verify(block: AisleVerificationRobot.() -> Unit): AisleVerificationRobot {
        return AisleVerificationRobot(composeTestRule).apply(block)
    }
}

class AisleVerificationRobot(private val composeTestRule: AndroidComposeTestRule<*, *>) {
    private val context = composeTestRule.activity

    fun aisleIsDisplayed(name: String) {
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }
    
    fun emptyStateIsDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.aisle_empty)).assertIsDisplayed()
    }
}

fun aisleRobot(composeTestRule: AndroidComposeTestRule<*, *>, block: AisleRobot.() -> Unit) =
    AisleRobot(composeTestRule).apply(block)
