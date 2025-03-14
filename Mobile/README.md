# PowderTask - Application Mobile

PowderTask est une application mobile développée en Kotlin pour gérer les tâches en équipe et contrôller la balance connectée.

---

## Fonctionnalités

- **Authentification sécurisée** : Connexion et inscription des utilisateurs.
- **Gestion des tâches** : Création, modification et suppression des tâches.
- **Notifications en temps réel** : Alertes pour les échéances des tâches.
- **Synchronisation avec le cloud** : Sauvegarde et synchronisation des données avec un serveur distant.
- **Ecoute sur des topics d'un broker MQTT**: Masse calculer par la balance, alerte, aide...

## Technologies utilisées

- **Langage** : Kotlin
- **Architecture** : MVVM (Model-View-ViewModel)
- **API** : API de gestion PowderTask Systems
- **Authentification** : Connexion sécurisé à l'API, JWT
- **MQTT ssl**

---

## Prérequis

Avant de commencer, assurez-vous d'avoir les éléments suivants :

- **Android Studio** : Version 2024.3.1.
- **SDK Android** : Version SDK 35.
- **URL API** : https://my-gin-api.quentinlecourt.com/
- **URL MQTT** : ssl://mqtt.powdertask.quentinlecourt.com:8883
  Aucun certificat n'est à ajouter, les certificats ssl de l'API et du MQTT sont reconnus n'ont donc pas besoin d'être intégrés au programme

---

## Installation

1. **Cloner le dépôt** :
   
   ```bash
   git clone https://github.com/ProjetTech2025-QuentinLecourt/PT-QL-PowderTask.git
   cd Mobile
   ```

2. **Ouvrir le projet dans Android Studio** :
   
   - Lancez Android Studio.
   
   - Sélectionnez **Open an Existing Project** et choisissez le dossier du projet.

3. **Configurer les clés API** :
   
   - Créez un fichier `build.gradle.kts` à la racine du projet.
   
   - Ajoutez-y l'URL de l'API :
     
     properties
     
     ```kotlin
     android {
         defaultConfig {
     // URL vers l'API
     buildConfigField("String", "API_BASE_URL", "\"https://my-gin-api.quentinlecourt.com/\"")
     }
     buildFeatures {
             buildConfig = true
     }
     ```

4. **Builder et exécuter l'application** :
   
   - Connectez un appareil Android ou utilisez un émulateur.
   
   - Cliquez sur **Run** (icône de lecture verte) dans Android Studio.

# 