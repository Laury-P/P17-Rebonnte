package com.openclassrooms.rebonnte.feature.medicine

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.ui.theme.RebonnteTheme
import com.openclassrooms.rebonnte.feature.medicine.list.ListMedicinesState
import com.openclassrooms.rebonnte.feature.medicine.list.MedicineContent
import org.junit.Rule
import org.junit.Test

class MedicineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testAisles = listOf(
        Aisle(aisleId = "A1", name = "Rayon A"),
        Aisle(aisleId = "A2", name = "Rayon B")
    )

    private val testMedicines = listOf(
        Medicine(medicineId = "1", name = "Doliprane", stock = 10, aisleId = "A1"),
        Medicine(medicineId = "2", name = "Aspegic", stock = 5, aisleId = "A2")
    )

    @Test
    fun testMedicineContent_Success_DisplaysList() {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineContent(
                    uiState = ListMedicinesState.Success(testMedicines),
                    searchQuery = "",
                    aisles = testAisles,
                    operationState = OperationState.Idle,
                    onSearchQueryChange = {},
                    onSortChange = {},
                    onAddMedicine = { _, _, _ -> },
                    onRetry = {},
                    onMedicineClick = {},
                    onSignOut = {}
                )
            }
        }

        // Vérifie le titre
        composeTestRule.onNodeWithText("Médicaments").assertIsDisplayed()

        // Vérifie les médicaments
        composeTestRule.onNodeWithText("Doliprane").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aspegic").assertIsDisplayed()

        // Vérifie les stocks
        composeTestRule.onNodeWithText("Stock: 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock: 5").assertIsDisplayed()
    }

    @Test
    fun testMedicineContent_Search_TriggersCallback() {
        var lastQuery = ""
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineContent(
                    uiState = ListMedicinesState.Success(testMedicines),
                    searchQuery = "",
                    aisles = testAisles,
                    operationState = OperationState.Idle,
                    onSearchQueryChange = { lastQuery = it },
                    onSortChange = {},
                    onAddMedicine = { _, _, _ -> },
                    onRetry = {},
                    onMedicineClick = {},
                    onSignOut = {}
                )
            }
        }

        // Saisit du texte dans la barre de recherche
        composeTestRule.onNodeWithText("Rechercher un médicament…").performTextInput("Doli")

        // Vérifie que le callback est appelé
        assert(lastQuery == "Doli")
    }

    @Test
    fun testMedicineContent_FabClick_OpensDialog() {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineContent(
                    uiState = ListMedicinesState.Success(emptyList()),
                    searchQuery = "",
                    aisles = testAisles,
                    operationState = OperationState.Idle,
                    onSearchQueryChange = {},
                    onSortChange = {},
                    onAddMedicine = { _, _, _ -> },
                    onRetry = {},
                    onMedicineClick = {},
                    onSignOut = {}
                )
            }
        }

        // Clique sur le FAB d'ajout
        composeTestRule.onNodeWithContentDescription("Ajouter un médicament").performClick()

        // Vérifie que le dialogue est ouvert
        composeTestRule.onNodeWithText("Nouveau médicament").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nom du médicament").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sélectionner le rayon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock initial").assertIsDisplayed()
    }

    @Test
    fun testMedicineContent_SortMenu_DisplaysOptions() {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineContent(
                    uiState = ListMedicinesState.Success(testMedicines),
                    searchQuery = "",
                    aisles = testAisles,
                    operationState = OperationState.Idle,
                    onSearchQueryChange = {},
                    onSortChange = {},
                    onAddMedicine = { _, _, _ -> },
                    onRetry = {},
                    onMedicineClick = {},
                    onSignOut = {}
                )
            }
        }

        // Clique sur le bouton de tri
        composeTestRule.onNodeWithContentDescription("Trier").performClick()

        // Vérifie les options du menu
        composeTestRule.onNodeWithText("Aucun tri").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trier par nom").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trier par stock").assertIsDisplayed()
    }

    @Test
    fun testMedicineContent_Error_DisplaysRetryButton() {
        var retryClicked = false
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineContent(
                    uiState = ListMedicinesState.Error("Erreur Réseau"),
                    searchQuery = "",
                    aisles = testAisles,
                    operationState = OperationState.Idle,
                    onSearchQueryChange = {},
                    onSortChange = {},
                    onAddMedicine = { _, _, _ -> },
                    onRetry = { retryClicked = true },
                    onMedicineClick = {},
                    onSignOut = {}
                )
            }
        }

        // Vérifie le message d'erreur
        composeTestRule.onNodeWithText("Erreur Réseau").assertIsDisplayed()

        // Clique sur le bouton de retry (le bouton est dans ErrorComponent)
        // On suppose que le bouton contient le texte "Réessayer" ou similaire
        composeTestRule.onNodeWithText("Retry").performClick()

        assert(retryClicked)
    }
}
