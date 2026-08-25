package com.openclassrooms.rebonnte.feature.medicine

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.medicine.detail.MedicineDetailContent
import com.openclassrooms.rebonnte.feature.medicine.detail.UiState
import org.junit.Rule
import org.junit.Test

class MedicineDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testMedicine = Medicine(
        medicineId = "med1",
        name = "Doliprane",
        stock = 10,
        aisleId = "A1",
        aisleName = "Cardiologie",
        histories = listOf(
            History(
                medicineId = "med1",
                details = "Initial Stock",
                timeStamp = 1700000000000L,
                userId = "user1"
            )
        )
    )

    @Test
    fun testMedicineDetailContent_Success_DisplaysInformation() {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    uiState = UiState.Success(testMedicine),
                    updateState = OperationState.Idle,
                    deleteState = OperationState.Idle,
                    onUpdateStock = { _, _ -> },
                    onDeleteMedicine = {},
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("medicine_detail_title").assertTextEquals("Doliprane")

        // Vérifie les informations de base (Labels + Valeurs)
        composeTestRule.onNodeWithText("Nom").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Doliprane").assertCountEquals(2) // title + name field

        composeTestRule.onNodeWithText("Rayon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cardiologie").assertIsDisplayed()

        // Vérifie le stock
        composeTestRule.onNodeWithText("Stock").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()

        // Vérifie l'historique
        composeTestRule.onNodeWithText("Initial Stock", substring = true).assertIsDisplayed()
    }

    @Test
    fun testMedicineDetailContent_Success_EmptyHistory() {
        val medicineNoHistory = testMedicine.copy(histories = emptyList())
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    uiState = UiState.Success(medicineNoHistory),
                    updateState = OperationState.Idle,
                    deleteState = OperationState.Idle,
                    onUpdateStock = { _, _ -> },
                    onDeleteMedicine = {},
                    onNavigateBack = {}
                )
            }
        }

        // Vérifie le message d'historique vide
        composeTestRule.onNodeWithText("Aucun historique pour ce médicament").assertIsDisplayed()
    }

    @Test
    fun testMedicineDetailContent_StockControls_TriggerCallbacks() {
        var isIncreaseClicked = false
        var isDecreaseClicked = false

        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    uiState = UiState.Success(testMedicine),
                    updateState = OperationState.Idle,
                    deleteState = OperationState.Idle,
                    onUpdateStock = { _, increase ->
                        if (increase) isIncreaseClicked = true else isDecreaseClicked = true
                    },
                    onDeleteMedicine = {},
                    onNavigateBack = {}
                )
            }
        }

        // Clique sur +
        composeTestRule.onNodeWithContentDescription("Augmenter le stock").performClick()
        assert(isIncreaseClicked)

        // Clique sur -
        composeTestRule.onNodeWithContentDescription("Diminuer le stock").performClick()
        assert(isDecreaseClicked)
    }

    @Test
    fun testMedicineDetailContent_DeleteClick_OpensDialog() {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    uiState = UiState.Success(testMedicine),
                    updateState = OperationState.Idle,
                    deleteState = OperationState.Idle,
                    onUpdateStock = { _, _ -> },
                    onDeleteMedicine = {},
                    onNavigateBack = {}
                )
            }
        }

        // Clique sur l'icône de suppression
        composeTestRule.onNodeWithContentDescription("Supprimer le médicament").performClick()

        // Vérifie que le dialogue d'alerte s'affiche
        composeTestRule.onNodeWithText("Suppression").assertIsDisplayed()
        composeTestRule.onNodeWithText("Voulez-vous supprimer ce médicament ? Cette action est irréversible.").assertIsDisplayed()
    }

    @Test
    fun testMedicineDetailContent_LoadingState_DisplaysLoader() {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    uiState = UiState.Loading,
                    updateState = OperationState.Idle,
                    deleteState = OperationState.Idle,
                    onUpdateStock = { _, _ -> },
                    onDeleteMedicine = {},
                    onNavigateBack = {}
                )
            }
        }

        // Le loader doit être présent
        composeTestRule.onNodeWithTag("medicine_detail_title").assertTextEquals("Détail du médicament")
    }

    @Test
    fun testMedicineDetailContent_ErrorState_DisplaysErrorMessage() {
        val errorMsg = "Erreur de chargement fatale"
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    uiState = UiState.Error(errorMsg),
                    updateState = OperationState.Idle,
                    deleteState = OperationState.Idle,
                    onUpdateStock = { _, _ -> },
                    onDeleteMedicine = {},
                    onNavigateBack = {}
                )
            }
        }

        // Vérifie le message d'erreur
        composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed()
    }
}
