package com.openclassrooms.rebonnte.feature.medicine

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.HiltTestActivity
import com.openclassrooms.rebonnte.feature.fakes.FakeAuthRepository
import com.openclassrooms.rebonnte.feature.fakes.FakeMedicineRepository
import com.openclassrooms.rebonnte.feature.robots.aisleRobot
import com.openclassrooms.rebonnte.feature.robots.authRobot
import com.openclassrooms.rebonnte.feature.robots.medicineRobot
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.feature.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.feature.navgraphs.RootNavGraph
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MedicineFlowIntegrationTest {

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
        
        // Seed initial data
        val pharmacie = Aisle(name = "Pharmacie", aisleId = "aisle_1")
        medicineRepository.seedAisles(listOf(pharmacie))
        medicineRepository.seedMedicines(
            listOf(
                Medicine(
                    medicineId = "med_1",
                    name = "Doliprane",
                    stock = 50,
                    aisleId = "aisle_1",
                    aisleName = "Pharmacie"
                )
            )
        )
    }

    @Test
    fun testNavigationFlow_AisleToMedicineDetail() {
        composeTestRule.setContent {
            RebonnteTheme {
                DestinationsNavHost(navGraph = RootNavGraph)
            }
        }

        // 1. Login
        authRobot(composeTestRule) {
            enterEmail("user@test.com")
            enterPassword("password")
            clickLogin()
        }

        // 2. Click on Aisle "Pharmacie"
        aisleRobot(composeTestRule) {
            clickAisle("Pharmacie")
        }

        // 3. Click on Medicine "Doliprane"
        medicineRobot(composeTestRule) {
            clickMedicine("Doliprane")
        } verify {
            // 4. Verify Detail Screen
            detailTitleIsDisplayed("Doliprane")
            stockValueIsDisplayed("50")
        }
    }

    @Test
    fun testCreateMedicineFlow_StartingFromMedicineScreen() {
        // Pré-authentification manuelle pour bypasser l'écran de login
        authRepository.currentUserId = "fake_user_id"

        composeTestRule.setContent {
            RebonnteTheme {
                val navController = rememberNavController()
                DestinationsNavHost(
                    navGraph = RootNavGraph,
                    navController = navController
                )
                // On navigue immédiatement vers l'écran Medicine
                navController.navigate(MedicineScreenDestination.route)
            }
        }

        // 1. Create Medicine (Directly on Medicine Screen)
        medicineRobot(composeTestRule) {
            clickAddMedicine()
            enterMedicineName("Ibuprofène")
            selectAisle("Pharmacie")
            enterInitialStock("20")
            clickSaveMedicine()
        } verify {
            // 2. Verify it's in the list
            medicineIsDisplayed("Ibuprofène")
        }
    }

    @Test
    fun testSearchMedicineFlow() {
        // Seed more data for search
        medicineRepository.seedMedicines(
            listOf(
                Medicine(medicineId = "m1", name = "Doliprane", stock = 10, aisleId = "a1", aisleName = "A"),
                Medicine(medicineId = "m2", name = "Ibuprofène", stock = 5, aisleId = "a1", aisleName = "A"),
                Medicine(medicineId = "m3", name = "Aspirine", stock = 20, aisleId = "a1", aisleName = "A")
            )
        )
        authRepository.currentUserId = "fake_user_id"

        composeTestRule.setContent {
            RebonnteTheme {
                val navController = rememberNavController()
                DestinationsNavHost(navGraph = RootNavGraph, navController = navController)
                navController.navigate(MedicineScreenDestination.route)
            }
        }

        medicineRobot(composeTestRule) {
            // 1. Initial check
            verify {
                medicineIsDisplayed("Doliprane")
                medicineIsDisplayed("Ibuprofène")
                medicineIsDisplayed("Aspirine")
            }

            // 2. Search "Ibu"
            enterSearchQuery("Ibu")
        } verify {
            // 3. Verify filtered list
            medicineIsDisplayed("Ibuprofène")
            medicineIsNotDisplayed("Doliprane")
            medicineIsNotDisplayed("Aspirine")
        }
    }
}
