package com.openclassrooms.rebonnte.feature.auth

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.HiltTestActivity
import com.openclassrooms.rebonnte.feature.fakes.FakeAuthRepository
import com.openclassrooms.rebonnte.feature.robots.aisleRobot
import com.openclassrooms.rebonnte.feature.robots.authRobot
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LogoutIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepository: FakeAuthRepository

    @Before
    fun setup() {
        hiltRule.inject()
        // Simuler un utilisateur déjà connecté
        authRepository.currentUserId = "fake_user_id"
    }

    @Test
    fun testLogoutFlow() {
        composeTestRule.setContent {
            RebonnteTheme {
                val navController = rememberNavController()
                DestinationsNavHost(
                    navGraph = RootNavGraph,
                    navController = navController
                )
                // Navigation immédiate vers AisleScreen car on simule être déjà loggé
                navController.navigate(AisleScreenDestination.route)
            }
        }

        // 1. Start at Aisle Screen and Logout
        aisleRobot(composeTestRule) {
            clickLogout()
        } verify {
            // 2. Verify we are back on LogScreen
            authRobot(composeTestRule) {
                verify { 
                    loginTitleIsDisplayed()
                }
                
                // 3. Test backstack resilience
                pressBack()
                
                // If the backstack was correctly cleared (inclusive pop), 
                // pressing back on LogScreen will finish the activity.
                // We verify the activity state rather than compose hierarchy.
                composeTestRule.waitForIdle()
                assertTrue(composeTestRule.activity.isFinishing || composeTestRule.activity.isDestroyed)
            }
        }
    }
}
