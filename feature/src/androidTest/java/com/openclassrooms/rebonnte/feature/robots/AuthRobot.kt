package com.openclassrooms.rebonnte.feature.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.openclassrooms.rebonnte.feature.R
import androidx.activity.ComponentActivity

class AuthRobot(private val composeTestRule: AndroidComposeTestRule<*, *>) {

    private val context = composeTestRule.activity

    fun enterEmail(email: String) {
        composeTestRule.onNodeWithText(context.getString(R.string.auth_email_label)).performTextInput(email)
    }

    fun enterPassword(password: String) {
        composeTestRule.onNodeWithText(context.getString(R.string.auth_password_label)).performTextInput(password)
    }

    fun clickLogin() {
        composeTestRule.onNodeWithText(context.getString(R.string.auth_login_button)).performClick()
    }
    
    fun clickToSignUp() {
        composeTestRule.onNodeWithText(context.getString(R.string.auth_no_account)).performClick()
    }

    infix fun verify(block: AuthVerificationRobot.() -> Unit): AuthVerificationRobot {
        return AuthVerificationRobot(composeTestRule).apply(block)
    }
}

class AuthVerificationRobot(private val composeTestRule: AndroidComposeTestRule<*, *>) {
    private val context = composeTestRule.activity

    fun errorIsDisplayed(error: String) {
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }
    
    fun loginTitleIsDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.auth_login_title)).assertIsDisplayed()
    }
}

fun authRobot(composeTestRule: AndroidComposeTestRule<*, *>, block: AuthRobot.() -> Unit) =
    AuthRobot(composeTestRule).apply(block)
