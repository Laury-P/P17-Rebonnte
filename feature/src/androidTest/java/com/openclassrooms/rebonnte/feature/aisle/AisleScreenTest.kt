package com.openclassrooms.rebonnte.feature.aisle

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.aisle.list.AisleContent
import com.openclassrooms.rebonnte.feature.aisle.list.ListAislesState
import org.junit.Rule
import org.junit.Test

class AisleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAisleContent_EmptyList_DisplaysEmptyMessage() {
        composeTestRule.setContent {
            AisleContent(
                uiState = ListAislesState.Success(emptyList()),
                operationState = OperationState.Idle,
                onAddAisle = {},
                onRetry = {},
                onAisleClick = {},
                onSignOut = {}
            )
        }

        // Vérifie le titre
        composeTestRule.onNodeWithText("Rayons").assertIsDisplayed()
        // Vérifie le message de liste vide
        composeTestRule.onNodeWithText("Aucun rayon pour le moment").assertIsDisplayed()
    }

    @Test
    fun testAisleContent_WithItems_DisplaysList() {
        val aisles = listOf(
            Aisle(aisleId = "1", name = "Rayon A"),
            Aisle(aisleId = "2", name = "Rayon B")
        )

        composeTestRule.setContent {
            AisleContent(
                uiState = ListAislesState.Success(aisles),
                operationState = OperationState.Idle,
                onAddAisle = {},
                onRetry = {},
                onAisleClick = {},
                onSignOut = {}
            )
        }

        // Vérifie que les items sont affichés
        composeTestRule.onNodeWithText("Rayon A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rayon B").assertIsDisplayed()
    }

    @Test
    fun testAisleContent_LoadingState_DisplaysLoader() {
        composeTestRule.setContent {
            AisleContent(
                uiState = ListAislesState.Loading,
                operationState = OperationState.Idle,
                onAddAisle = {},
                onRetry = {},
                onAisleClick = {},
                onSignOut = {}
            )
        }

        // Vérifie la présence du loader (généralement via content description si défini)
        // Comme c'est un composant commun, on peut chercher par tag ou si on connaît sa description
        // Si LoadingComponent n'a pas de texte, on peut au moins vérifier que la liste n'est pas là
        composeTestRule.onNodeWithText("Aucun rayon pour le moment").assertDoesNotExist()
    }

    @Test
    fun testAisleContent_FabClick_OpensDialog() {
        composeTestRule.setContent {

            AisleContent(
                uiState = ListAislesState.Success(emptyList()),
                operationState = OperationState.Idle,
                onAddAisle = {},
                onRetry = {},
                onAisleClick = {},
                onSignOut = {}
            )

        }

        // Clique sur le FAB d'ajout
        composeTestRule.onNodeWithContentDescription("Ajouter un nouveau rayon").performClick()

        // Vérifie que le dialogue est ouvert
        composeTestRule.onNodeWithText("Nouveau rayon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nom du rayon").assertIsDisplayed()
    }

    @Test
    fun testAisleContent_ItemClick_TriggersCallback() {
        var clickedAisleId: String? = null
        val aisles = listOf(Aisle(aisleId = "123", name = "Test Aisle"))

        composeTestRule.setContent {

            AisleContent(
                uiState = ListAislesState.Success(aisles),
                operationState = OperationState.Idle,
                onAddAisle = {},
                onRetry = {},
                onAisleClick = { clickedAisleId = it },
                onSignOut = {}
            )

        }

        // Clique sur l'item
        composeTestRule.onNodeWithText("Test Aisle").performClick()

        // Vérifie que le callback a été appelé avec le bon ID
        assert(clickedAisleId == "123")
    }
}
