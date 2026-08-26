# 💊 Rebonnte — Gestion de Stock de Médicaments

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84.svg?logo=android)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg?logo=firebase)](https://firebase.google.com/)

> **Projet OpenClassrooms (Projet 17)** — *Améliorez une application Android tout au long de son cycle de vie.*

---

## 📌 Présentation du Projet

**Rebonnte** est une application Android de gestion des stocks de médicaments. Ce projet s'inscrit dans le cadre du parcours **Développeur d'application Android** chez OpenClassrooms. 

L'objectif principal est d'assurer le cycle de vie complet de l'application :
* Reprise et correction d'une base de code obsolète.
* Refactoring architectural (Modularisation, Clean Architecture, MVVM).
* Optimisation des performances selon les principes du **Green Code**.
* Sécurisation des données et mise en place d'une chaîne **CI/CD**.

L'application permet aux pharmaciens et gestionnaires de suivre l'inventaire en temps réel, de consulter un historique fiable des modifications et de gérer les mouvements de stock de manière fluide et éco-responsable.

---
## 📸 Aperçu

| Liste des médicaments | Détails stock & Historique |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/bd87b2c7-fc84-40c3-a85b-426ff7efd61d" width="280" alt="Liste des médicaments" /> | <img src="https://github.com/user-attachments/assets/d298b7cc-2318-476a-90cb-1a57e08fb439" width="280" alt="Détails du stock et historique" /> |

<br />

<div align="center">
  <!-- Troisième image centrée en dessous -->
  <figure style="display: inline-block; margin: 10px;">
    <img src="https://github.com/user-attachments/assets/42af42cb-383e-4a1a-8c91-279fc6548732" width="1400" alt="Analyse Sonar" />
    <figcaption><b>Analyse sonar via pipeline CI</b></figcaption>
  </figure>
</div>

---

## 🎯 Objectifs Pédagogiques

* **Correction & Stabilité** : Traque et résolution des comportements inattendus, fuites de mémoire et crashs critiques.
* **Optimisation Green Code** : Réduction de l'empreinte carbone et mémoire de l'application.
* **Stratégie de Tests** : Écriture et exécution de tests unitaires et d'intégration.
* **Intégration Continue (CI/CD)** : Automatisation du build, de l'analyse et du déploiement.
* **Documentation Technique** : Rédaction d'un ensemble de tâches techniques pour réaliser le project et autodocumentation du code pour faciliter la maintenance.

---

## 🛠 Stack Technique

* **Langage** : Kotlin
* **Interface Utilisateur** : Jetpack Compose (Material 3)
* **Architecture** : Clean Architecture + MVVM
* **Modularisation** : Multimodule (`:app`, `:core`, `:data`, `:feature`)
* **Injection de Dépendances** : Hilt
* **Asynchronisme** : Coroutines & Flow
* **Backend & Services (Firebase)** :
  * **Authentication** : Gestion sécurisée des comptes opérateurs.
  * **Cloud Firestore** : Base NoSQL avec indexation et filtrage côté serveur.
* **CI/CD** : GitHub Actions (Build, Unit + Integration Tests, Coverage avec Jacoco, Analyse avec Sonar et Firebase App Distribution).

---

## 🧩 Fonctionnalités

- 📦 **Gestion des Stocks** : Incrémentation/décrémentation sécurisée (+1/-1).
- 🕒 **Historique Traçable** : Journal détaillé des actions (email de l'opérateur, horodatage, modification apportée).
- ➕ **Ajout & Suppression** : Formulaire de création de références et retrait définitif du stock.
- 🔍 **Filtrage Optimisé** : Tri direct via les index Firestore pour économiser la bande passante et l'énergie.
- ♿ **Accessibilité** : Prise en charge de TalkBack.

---

## 🏗 Architecture & Qualité

🌱 **Green Code (Éco-conception)**
* Déplacement de la logique lourde hors du Thread Principal (`Dispatchers.Default` / `IO`) via les coroutines.
* Réduction de la consommation réseau grâce à la gestion du cache Firestore.

🧪 **Qualité & Tests**
* **Tests Unitaires** : Logic métier et ViewModels couverts avec **JUnit 5** et **MockK**.
* **Tests d'interfaces** : Validation du rendu visuel et du comportement des composants UI via des écrans *stateless*.
* **Tests d'Intégration** : Validation du flux de données entre modules et services externes.
* **Analyse Statique** : Contrôle continu de la qualité du code avec **Android Lint** et **SonarQube**.

---

## ⚙️ Installation & Configuration

Pour des raisons de sécurité, les fichiers de configuration Firebase ne sont pas inclus dans ce dépôt.

1. **Cloner le projet** :
   ```bash
   git clone https://github.com/votre-compte/rebonnte.git
   ```
2. **Configuration Firebase** :
   * Créez un projet sur la [Console Firebase](https://console.firebase.google.com/).
   * Ajoutez une application Android avec le nom de package du projet.
   * Téléchargez le fichier `google-services.json` et déplacez-le dans le dossier `app/`.
   * Activez les modules **Authentication** (Mode Email/Mot de passe) et **Cloud Firestore**.
3. **CI/CD (Optionnel)** :
   * Ajoutez vos variables secrètes dans GitHub (`KEYSTORE_BASE64`, `FIREBASE_TOKEN`, etc.) pour exécuter les workflows GitHub Actions.

---

## 🔍 Limites et Perspectives

* [ ] **Mode Hors Ligne complet** : Intégration d'une base de données locale **Room** pour les zones sans réseau (entrepôts).
* [ ] **Suppression sécurisée** : Utilisation de Cloud Functions (Firebase) pour gérer la suppression d'un médicament côté serveur plutôt que directement depuis l'application.

---

## 👤 Auteur

**Laury PRIN** — *Développeur Android*  
Projet réalisé dans le cadre de la formation Développeur d'application Android chez OpenClassrooms.
