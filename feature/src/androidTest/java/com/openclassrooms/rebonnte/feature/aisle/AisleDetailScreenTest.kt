package com.openclassrooms.rebonnte.feature.aisle

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.aisle.detail.AisleDetailContent
import com.openclassrooms.rebonnte.feature.aisle.detail.UiState
import org.junit.Rule
import org.junit.Test

class AisleDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAisleDetailContent_Success_DisplaysAisleNameAndMedicines() {
        // GIVEN
        val aisle = Aisle(aisleId = "1", name = "Cardiologie")
        val medicines = listOf(
            Medicine("m1", "Doliprane", stock = 10, aisleId = "1"),
            Medicine("m2", "Aspegic", stock = 3, aisleId = "1")
        )

        // WHEN
        composeTestRule.setContent {
            RebonnteTheme {
                AisleDetailContent(
                    uiState = UiState.Success(aisle, medicines),
                    onMedicineClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // THEN
        // Attend que le titre apparaisse avec le bon texte
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("aisle_detail_title")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // Vérifie le contenu du titre par son tag
        composeTestRule.onNodeWithTag("aisle_detail_title")
            .assertTextEquals("Cardiologie")
            .assertIsDisplayed()

        // Vérifie que les médicaments sont affichés
        composeTestRule.onNodeWithText("Doliprane").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock: 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aspegic").assertIsDisplayed()

        // Vérifie que le stock critique (3) est bien présent
        composeTestRule.onNodeWithText("Stock: 3").assertIsDisplayed()
    }

    @Test
    fun testAisleDetailContent_Empty_DisplaysEmptyMessage() {
        // GIVEN
        val aisle = Aisle(aisleId = "1", name = "Cardiologie")

        // WHEN
        composeTestRule.setContent {
            RebonnteTheme {
                AisleDetailContent(
                    uiState = UiState.Success(aisle, emptyList()),
                    onMedicineClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // THEN
        composeTestRule.onNodeWithText("Aucun médicament dans ce rayon").assertIsDisplayed()
    }

    @Test
    fun testAisleDetailContent_Loading_DisplaysLoading() {
        // WHEN
        composeTestRule.setContent {
            RebonnteTheme {
                AisleDetailContent(
                    uiState = UiState.Loading,
                    onMedicineClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // THEN
        // On vérifie que le titre par défaut est là (puisqu'on n'a pas encore le nom du rayon)
        composeTestRule.onNodeWithTag("aisle_detail_title")
            .assertTextEquals("Détail du rayon")
            .assertIsDisplayed()
    }

    @Test
    fun testAisleDetailContent_Error_DisplaysError() {
        val errorMessage = "Failed to load details"
        // WHEN
        composeTestRule.setContent {
            RebonnteTheme {
                AisleDetailContent(
                    uiState = UiState.Error(errorMessage),
                    onMedicineClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // THEN
        composeTestRule.onNodeWithText(errorMessage, substring = true).assertIsDisplayed()
    }

    @Test
    fun testAisleDetailContent_BackClick_TriggersCallback() {
        // GIVEN
        var backClicked = false
        val aisle = Aisle(aisleId = "1", name = "Cardiologie")

        // WHEN
        composeTestRule.setContent {
            RebonnteTheme {
                AisleDetailContent(
                    uiState = UiState.Success(aisle, emptyList()),
                    onMedicineClick = {},
                    onNavigateBack = { backClicked = true }
                )
            }
        }

        // THEN
        // Utilise la description extraite dans strings.xml
        composeTestRule.onNodeWithContentDescription("Retourner à la liste").performClick()
        assert(backClicked)
    }

    @Test
    fun testAisleDetailContent_MedicineClick_TriggersCallback() {
        // GIVEN
        var clickedId: String? = null
        val aisle = Aisle(aisleId = "1", name = "Cardiologie")
        val medicine = Medicine("m1", "Doliprane", stock = 10, aisleId = "1")

        // WHEN
        composeTestRule.setContent {
            RebonnteTheme {
                AisleDetailContent(
                    uiState = UiState.Success(aisle, listOf(medicine)),
                    onMedicineClick = { clickedId = it },
                    onNavigateBack = {}
                )
            }
        }

        // THEN
        composeTestRule.onNodeWithText("Doliprane").performClick()
        assert(clickedId == "m1")
    }
}
