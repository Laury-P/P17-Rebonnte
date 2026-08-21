package com.openclassrooms.rebonnte.feature.resilience

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.HiltTestActivity
import com.openclassrooms.rebonnte.feature.fakes.FakeAuthRepository
import com.openclassrooms.rebonnte.feature.fakes.FakeMedicineRepository
import com.openclassrooms.rebonnte.feature.robots.aisleRobot
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.feature.destinations.AisleScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ResilienceIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepository: FakeAuthRepository

    @Inject
    lateinit var medicineRepository: FakeMedicineRepository

    @Before
    fun setup() {
        hiltRule.inject()
        // Simuler une session active
        authRepository.currentUserId = "fake_user_id"
    }

    @Test
    fun testResilienceFlow_RetryAfterError() {
        // 1. Activer l'erreur globale sur le repository AVANT setContent
        medicineRepository.shouldReturnError = true

        composeTestRule.setContent {
            RebonnteTheme {
                val navController = rememberNavController()
                DestinationsNavHost(
                    navGraph = RootNavGraph,
                    navController = navController
                )
                // On force la navigation vers AisleScreen
                navController.navigate(AisleScreenDestination.route)
            }
        }

        // 2. Vérifier que l'écran d'erreur s'affiche (on attend le bouton Retry)
        aisleRobot(composeTestRule) {
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodes(hasText("Retry")).fetchSemanticsNodes().isNotEmpty()
            }
        } verify {
            errorIsDisplayed()
        }

        // 3. Désactiver l'erreur, ajouter une donnée et cliquer sur Retry
        medicineRepository.shouldReturnError = false
        medicineRepository.seedAisles(listOf(Aisle("Pharmacie", "1")))

        aisleRobot(composeTestRule) {
            clickRetry()
        } verify {
            // 4. Vérifier que les données sont enfin chargées
            aisleIsDisplayed("Pharmacie")
        }
    }
}
