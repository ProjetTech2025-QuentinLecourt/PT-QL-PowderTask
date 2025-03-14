# Balance Connectée ESP32

Ce projet consiste en une balance connectée utilisant un ESP32, un module HX711 pour la lecture des données du capteur de poids, et une communication MQTT pour envoyer les données à un broker. Le projet inclut également une interface utilisateur via un écran tactile géré par la classe `MyStone`.

## Table des matières

- [Balance Connectée ESP32](#balance-connectée-esp32)
  - [Table des matières](#table-des-matières)
  - [Fonctionnalités](#fonctionnalités)
  - [Matériel nécessaire](#matériel-nécessaire)
  - [Installation](#installation)
  - [Utilisation](#utilisation)

## Fonctionnalités

- Lecture du poids via le module HX711.
- Affichage du poids sur un écran tactile.
- Envoi des données de poids via MQTT à un broker.
- Connexion sécurisée au broker MQTT via TLS.

## Matériel nécessaire

- ESP32
- Module HX711
- Capteur de poids (cellule de charge)
- Écran tactile compatible avec la classe `MyStone`
- Câbles de connexion
- Alimentation pour l'ESP32 et le capteur de poids

## Installation

1. **Cloner le dépôt**:
   
   ```bash
   git clone https://github.com/ProjetTech2025-QuentinLecourt/PT-QL-PowderTask.git
   cd ./PowderTask_Blance
   ```

2. **Installation des dépendances**:
   
    Assurez-vous d'avoir installé l'IDE Arduino ou PlatformIO.
    Installez les bibliothèques suivantes via le gestionnaire de bibliothèques de l'IDE Arduino ou PlatformIO:
   
   - PubSubClient : knolleary/PubSubClient @ ^2.8
   - HX711 : bogde/HX711 @ ^0.7.5
   - AsyncMqttClient : heman/AsyncMqttClient-esphome @ ^2.1.0

3. **Configurer le projet**:
   
   - Ouvrez le fichier balance_connectee.ino dans l'IDE Arduino ou PlatformIO.
   - Modifiez les constantes dans le code pour correspondre à votre configuration réseau et MQTT:
     
     ```
        const char* ssid = "VOTRE_SSID";
        const char* password = "VOTRE_MOT_DE_PASSE";
        const char* mqtt_server = "VOTRE_BROKER_MQTT";
        const int mqtt_port = 8883;
        const char* mqtt_user = "VOTRE_UTILISATEUR_MQTT";
        const char* mqtt_password = "VOTRE_MOT_DE_PASSE_MQTT";
     ```

4. **Téléverser le code:**
- Connectez votre ESP32 à votre ordinateur via USB.
- Sélectionnez le bon port et le modèle de carte dans l'IDE Arduino ou PlatformIO.
- Téléversez le code sur l'ESP32.

## Configuration

- **Calibration du capteur de poids:**
  - Ajustez le facteur d'échelle dans le code pour calibrer le capteur de poids:
    
    ```
    scale.set_scale(21.7074f); // Ajustez cette valeur en fonction de votre capteur
    ```
  - Utilisez la méthode scale.tare() pour réinitialiser la tare.
- **Configuration de l'écran tactile:**
  - Assurez-vous que l'écran tactile est correctement connecté et configuré dans la classe MyStone.

## Utilisation

1. **Démarrer la balance:**
   
   - Une fois le code téléversé, la balance se connectera automatiquement au réseau Wi-Fi et au broker MQTT.
     Par la suite, ça sera à l'utilisateur de se connecter.
   - Le poids mesuré sera affiché sur l'écran tactile et envoyé au broker MQTT.
   - **ATTENTION!** Les capteurs de poids (cellules de charge) ne suporte pas plus de 50 Kg.

2. **Surveillance des données:**
   
   - Vous pouvez surveiller les données de poids via le broker MQTT en vous abonnant au topic test.

3. **Dépannage:**
   
   - Si la balance ne se connecte pas au Wi-Fi ou au broker MQTT, vérifiez les messages de débogage dans le moniteur série.