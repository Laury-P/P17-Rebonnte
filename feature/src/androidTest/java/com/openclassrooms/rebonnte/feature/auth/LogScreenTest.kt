package com.openclassrooms.rebonnte.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import org.junit.Rule
import org.junit.Test

class LogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLogContent_InitialState_IsLoginMode() {
        composeTestRule.setContent {
            LogContent(
                operationState = OperationState.Idle,
                onSignIn = { _, _ -> },
                onSignUp = { _, _, _ -> }
            )
        }

        // Vérifie les textes du mode Connexion
        composeTestRule.onNodeWithText("Connexion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mot de passe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Se connecter").assertIsDisplayed()

        // Le champ "Nom complet" ne doit PAS être là en mode connexion
        composeTestRule.onNodeWithText("Nom complet").assertDoesNotExist()
    }

    @Test
    fun testLogContent_SwitchToSignupMode_ShowsExtraFields() {
        composeTestRule.setContent {
            LogContent(
                operationState = OperationState.Idle,
                onSignIn = { _, _ -> },
                onSignUp = { _, _, _ -> }
            )
        }

        // Clique sur le bouton pour basculer en mode Inscription
        composeTestRule.onNodeWithText("Pas de compte ? S'inscrire").performClick()

        // Vérifie que les éléments d'inscription apparaissent
        composeTestRule.onNodeWithText("Inscription").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nom complet").assertIsDisplayed()
        composeTestRule.onNodeWithText("S'inscrire").assertIsDisplayed()
    }

    @Test
    fun testLogContent_PasswordVisibilityToggle() {
        composeTestRule.setContent {
            LogContent(
                operationState = OperationState.Idle,
                onSignIn = { _, _ -> },
                onSignUp = { _, _, _ -> }
            )
        }

        // Par défaut, l'icône de visibilité (content description)
        composeTestRule.onNodeWithContentDescription("Afficher le mot de passe").assertIsDisplayed()

        // Clique sur l'œil
        composeTestRule.onNodeWithContentDescription("Afficher le mot de passe").performClick()

        // Vérifie que l'icône a changé
        composeTestRule.onNodeWithContentDescription("Cacher le mot de passe").assertIsDisplayed()
    }

    @Test
    fun testLogContent_ShowsLoadingIndicator_WhenStateIsLoading() {
        composeTestRule.setContent {
            LogContent(
                operationState = OperationState.Loading,
                onSignIn = { _, _ -> },
                onSignUp = { _, _, _ -> }
            )
        }

        // Vérifie la présence de l'indicateur de chargement
        composeTestRule.onNodeWithContentDescription("Chargement en cours").assertIsDisplayed()

        // Le bouton de connexion doit disparaître pendant le chargement (remplacé par l'indicateur)
        composeTestRule.onNodeWithText("Se connecter").assertDoesNotExist()
    }

    @Test
    fun testLogContent_ShowsErrorMessage_WhenStateIsError() {
        val errorMsg = "Email invalide"
        composeTestRule.setContent {
            LogContent(
                operationState = OperationState.Error(errorMsg),
                onSignIn = { _, _ -> },
                onSignUp = { _, _, _ -> }
            )
        }

        // Vérifie que le message d'erreur s'affiche
        composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed()
    }
}