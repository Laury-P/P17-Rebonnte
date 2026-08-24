package com.openclassrooms.rebonnte.feature.auth

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.openclassrooms.rebonnte.feature.HiltTestActivity
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.fakes.FakeAuthRepository
import com.openclassrooms.rebonnte.feature.robots.aisleRobot
import com.openclassrooms.rebonnte.feature.robots.authRobot
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LogIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepository: FakeAuthRepository

    @Before
    fun setup() {
        hiltRule.inject()
        composeTestRule.setContent {
            RebonnteTheme {
                DestinationsNavHost(navGraph = RootNavGraph)
            }
        }
    }

    @Test
    fun testLoginFlow_Success() {
        authRobot(composeTestRule) {
            enterEmail("test@example.com")
            enterPassword("password123")
            clickLogin()
        } verify {
            // After successful login, we should be on AisleScreen
            aisleRobot(composeTestRule) {
                verify { aisleIsDisplayed("Rayons") }
            }
        }
    }

    @Test
    fun testLoginFlow_Failure() {
        // Prepare fake to return error
        authRepository.shouldReturnError = true

        authRobot(composeTestRule) {
            enterEmail("wrong@example.com")
            enterPassword("wrong")
            clickLogin()
        } verify {
            // Check if error message is displayed
            // The error message from FakeAuthRepository is "Auth Error"
            errorIsDisplayed("Auth Error")
        }
    }

    @Test
    fun testSwitchToSignUpAndBack() {
        authRobot(composeTestRule) {
            clickToSignUp()
        } verify {
            // Should see the signup title
            composeTestRule.onNodeWithText("Nom complet").assertExists()
        }
    }
}
