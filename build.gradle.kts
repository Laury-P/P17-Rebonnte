// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Le moteur principal de l'application Android (AGP)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Les plugins Kotlin pour compiler le code et l'interface Compose
    alias(libs.plugins.kotlin.compose) apply false

    // Les services Google (indispensable pour lier Firebase à ton projet)
    alias(libs.plugins.google.services) apply false

    // L'injection de dépendances (Hilt) et son outil d'analyse (KSP)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false

}