# Dashbord Grafana

Souscription au broker MQTT et gestion des erreurs sur un dashboard.

## Table des matières

- [Pré-requis](#pr%C3%A9-requis)

- [Installation des outils](#installation-des-outils)
  
  - [Installation de Node-RED](#installation-de-node-red)
  
  - [Installation d'InfluxDB](#installation-dinfluxdb)
  
  - [Installation de Grafana](#installation-de-grafana)

- [Configuration](#configuration)
  
  - [Configurer Node-RED](#configurer-node-red)
  
  - [Configurer Grafana pour une jauge](#configurer-grafana-pour-une-jauge)

- [FAQ: Images en Markdown](#faq-images-en-markdown)

---

## Pré-requis

- Un VPS sous Linux.

- Accès root ou utilisateur avec privilèges sudo.

- Un broker MQTT opérationnel (par exemple Mosquitto).

- Ouvrir les ports 8086, 1880 et 3000 en tcp

---

## Installation des outils

### Installation de Node-RED

1. **Mettre à jour les paquets :**
   
   `sudo apt update && sudo apt upgrade -y`

2. **Installer Node.js  (si pas déjà fait):**
   
   `voir sur le site officiel pour choisir une verion stable`

3. **Installer Node-RED :**
   
   `sudo npm install -g --unsafe-perm node-red`

4. **Lancer Node-RED au démarrage :**
   
   `sudo npm install -g pm2 pm2 start $(which node-red) -- -v pm2 save pm2 startup` ou installer screen : `sudo apt install screen` puis `screen -S node-red` afin de lancer une session node red.

5. **Accéder à l’interface :**  
   Rendez-vous à l’adresse suivante dans votre navigateur : `http://<VPS_IP>:1880`.

---

### Installation d'InfluxDB

1. **Ajouter le dépôt :**
   
   `wget -qO- https://repos.influxdata.com/influxdb.key | sudo tee /etc/apt/trusted.gpg.d/influxdb.asc`
   
    ` echo "deb https://repos.influxdata.com/debian bullseye stable" | sudo tee /etc/apt/sources.list.d/influxdb.list`

2. **Installer InfluxDB :**
   
   `sudo apt update sudo apt install influxdb`

3. **Lancer le service :**
   
   `sudo systemctl start influxdb sudo systemctl enable influxdb`

4. **Créer une base de données :**
   
   `influx > CREATE DATABASE ma_base_de_donnees (Facultatif: CREATE USER usager WITH PASSWORD 'patate123' > GRANT ALL ON ma_base_de_donnees TO usager)`

---

### Installation de Grafana

1. **Ajouter le dépôt Grafana :**
   
   `wget -q -O - https://packages.grafana.com/gpg.key | sudo apt-key add -``echo "deb https://packages.grafana.com/oss/deb stable main" | sudo tee /etc/apt/sources.list.d/grafana.list`

2. **Installer Grafana :**
   
   `sudo apt update sudo apt install grafana`

3. **Lancer et activer Grafana :**
   
   `sudo systemctl start grafana-server sudo systemctl enable grafana-server`

4. **Accéder à l’interface :**  
   Rendez-vous à l’adresse suivante dans votre navigateur : `http://<VPS_IP>:3000`.
   
   - Identifiants par défaut : **admin/admin** (modifiez le mot de passe après la première connexion).

---

## Configuration

### Configurer Node-RED

1. **Ajouter le plugin MQTT :**
   
   - Dans l’interface Node-RED, allez dans "Gérer les palettes" > "Installer".
   
   - Recherchez et installez le module `node-red-contrib-mqtt-broker`.

2. **Créer un flux :**
   
   - Ajoutez un nœud MQTT pour souscrire au topic souhaité.
   
   - Connectez ce nœud à un nœud "Debug" pour vérifier les données reçues.

3. **Envoyer les données à InfluxDB :**
   
   - Installez le module `node-red-contrib-influxdb`.
   
   - Configurez une connexion à votre base de données InfluxDB avec les identifiants créés précédemment.
   
   - Connectez les données MQTT au nœud InfluxDB pour les écrire dans la base.
     
     Voici un exemple des noeuds utilisés pour écouter sur le topic /scale/1/status/online et /scale/1/status/details et mettre les données dans les tables
     
     Si vous utiliser un JSON dans votre message MQTT, utiliser un noeud "change", expression json et dans JSONata :
     {
     
        "accelerometerStatus":msg.payload.accelerometerStatus,
     
        "accX":msg.payload.accX,
     
        "accY":msg.payload.accY,
     
        "accZ":msg.payload.accZ,
     
        "accNorm":msg.payload.accNorm,
     
        "weightSensorStatus":msg.payload.weightSensorStatus,
     
        "weightDetected":msg.payload.weightDetected,
     
        "calibrationIndex":msg.payload.calibrationIndex,
     
        "display":msg.payload.display,
     
        "datetimeDelivery":msg.payload.datetimeDelivery
     
     }
     
     Sinon prendre le champ JSON qui convient
     
     <img src="file:///C:/Users/quent/AppData/Roaming/marktext/images/2025-05-15-12-24-11-image.png" title="" alt="" width="554">

---

### Configurer Grafana pour une jauge

1. **Ajouter la source de données :**
   
   - Allez dans "Configuration" > "Data Sources".
   
   - Cliquez sur "Add data source" et sélectionnez "InfluxDB".
   
   - Configurez la connexion en entrant l’URL de votre serveur InfluxDB, le nom de la base (`ma_base_de_donees`), et les identifiants s'il y a (`usager/patate123`).

2. **Créer un tableau de bord :**
   
   - Allez dans "Create" > "Dashboard".
   
   - Ajoutez un panneau (panel) et sélectionnez "Gauge" comme type de visualisation.

3. **Configurer la jauge :**
   
   - Dans l’onglet "Query", entrez une requête InfluxQL ou Flux pour récupérer vos données.
   
   - Ajustez les options de la jauge (plage de valeurs, seuils, etc.).
     
     ![](C:\Users\quent\AppData\Roaming\marktext\images\2025-05-15-12-28-10-image.png)
     
     ![](C:\Users\quent\AppData\Roaming\marktext\images\2025-05-15-12-28-36-image.png)


