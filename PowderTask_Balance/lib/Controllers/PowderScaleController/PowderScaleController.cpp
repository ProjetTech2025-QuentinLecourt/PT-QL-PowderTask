#include "PowderScaleController.h"

PowderScaleController::PowderScaleController(controllerInit init) : timeClient(ntpUDP, "pool.ntp.org")
{
    rxGpio = init.rx;
    txGpio = init.tx;
    stoneSpeed = init.stoneSpeed;

    sclale_dt = init.dt;
    scale_sck = init.sck;

    scale = init.scale;
    tare = init.tare;

    ssid = init.ssid;
    password = init.password;

    // MQTT
    mqtt_server = init.mqtt_server;
    mqtt_port = init.mqtt_port;
    mqtt_user = init.mqtt_user;
    mqtt_password = init.mqtt_password;
    scaleClass = new Scale(sclale_dt, scale_sck);
    myStone = new MyStone(stoneSpeed, SERIAL_8N1, rxGpio, txGpio);
    wifiClient = new WiFiClientSecure();
    mqttClient = new PubSubClient(*wifiClient);
    accelerometer = new MPU9250_asukiaaa();
}

PowderScaleController::~PowderScaleController()
{
    delete scaleClass;
    delete myStone;
    delete wifiClient;
    delete mqttClient;
}

/**
 * @brief Initialise au premier démarrage les variables stockées vides.
 */
void PowderScaleController::initEEPROM(bool forced)
{
    // Initialisation des offsets de connexion internet
    if ((EEPROM.read(SSID_OFFSET) == 0xFF) || forced)
    {
        saveString(SSID_OFFSET, "");
        saveString(WIFI_PASSWORD_OFFSET, "");
    }
    // Initialisation des offsets de connexion utilisateur
    if ((EEPROM.read(EMAIL_OFFSET) == 0xFF) || forced)
    {
        saveString(EMAIL_OFFSET, "");
        saveString(LOGIN_PASSWORD_OFFSET, "");
    }
    // Initialisation de l'offset du JWT
    if ((EEPROM.read(JWT_OFFSET) == 0xFF) || forced)
    {
        saveString(JWT_OFFSET, "");
    }
    // Initialisation de l'offset de la langue
    if ((EEPROM.read(LANGUAGE_OFFSET) == 0xFF) || forced)
    {
        saveEnum(LANGUAGE_OFFSET, FRENCH);
    }
    // Initialisation de l'offset de l'unité de la masse
    if ((EEPROM.read(WEIGHT_UNITE_OFFSET) == 0xFF) || forced)
    {
        saveEnum(WEIGHT_UNITE_OFFSET, UNITY_KG);
    }
    // Initialisation de l'offset d'installation
    if ((EEPROM.read(INSTALLATION_OFFSET) == 0xFF) || forced)
    {
        saveEnum(WEIGHT_UNITE_OFFSET, NOT_INSTALL);
    }
}

/**
 * @brief SVérifie si la chaîne de caractère est vide ou non conforme
 *
 * @param str Chaîne de caractères
 */
bool PowderScaleController::isStringValid(const String &str)
{
    if (str.length() == 0)
        return false;
    for (unsigned int i = 0; i < str.length(); i++)
    {
        if (!isprint(str[i]))
            return false;
    }
    return true;
}

/**
 * @brief Sauvegarde une chaîne de caractères dans l'EEPROM
 *
 * @param offset Adresse de départ dans l'EEPROM pour la sauvegarde
 * @param str Chaîne de caractères à sauvegarder
 */
void PowderScaleController::saveString(int offset, const String &str)
{
    for (int i = 0; i < str.length(); i++)
    {
        EEPROM.write(offset + i, str[i]);
    }
    EEPROM.write(offset + str.length(), '\0');
    EEPROM.commit();
}

/**
 * @brief Lit une chaîne de caractères depuis l'EEPROM
 *
 * @param offset Adresse de départ dans l'EEPROM pour la lecture
 * @return String La chaîne de caractères lue
 */
String PowderScaleController::readString(int offset)
{
    String str;
    char c;
    int i = 0;
    while ((c = EEPROM.read(offset + i)) != '\0' && i < 256)
    { // Limite de sécurité
        str += c;
        i++;
    }
    return (i > 0) ? str : "";
}

/**
 * @brief Sauvegarde une valeur enum (8 bits non signés) dans l'EEPROM
 *
 * @param offset Adresse dans l'EEPROM pour la sauvegarde
 * @param value Valeur enum à sauvegarder (uint8_t)
 */
void PowderScaleController::saveEnum(int offset, uint8_t value)
{
    EEPROM.write(offset, value);
    EEPROM.commit();
}

/**
 * @brief Lit une valeur enum (8 bits non signés) depuis l'EEPROM
 *
 * @param offset Adresse dans l'EEPROM pour la lecture
 * @return uint8_t La valeur enum lue
 */
uint8_t PowderScaleController::readEnum(int offset)
{
    return EEPROM.read(offset);
}

bool PowderScaleController::init()
{
    if (myStone == nullptr)
    {
        return false;
    }
    // Verification de la communication avec le HX711
    if (scaleClass == nullptr)
    {
        displayModal("Erreur d'instanciation", "Impossible d'instancier la classe de la balance !", "");
        return false;
    }
    if (!scaleClass->init(400, 100))
    {
        displayModal("Erreur d'initialisation", "Impossible d'intialiser la balance !", "");
        return false;
    }
    // Suppression de la classe Scale suite à la verification
    // delete scaleClass;
    // scaleClass = nullptr;
    weightSensorFunctionnal = true;
    

    if (!beginAccelerometer())
    {
        displayModal("Erreur d'initialisation", "L'accelerometre ne repond pas !", "Impossible de s'en servir");
        return false;
    }

    EEPROM.begin(EEPROM_SIZE);
    (readEnum(INSTALLATION_OFFSET) == NOT_INSTALL) ? initEEPROM(true) : initEEPROM(false);

    // Se connecter au Wi-Fi
    connectToWiFi();

    Serial.println("WiFi connected, initializing NTP");
    initNTP();

    // Configurer le client MQTT
    mqttClient->setServer(mqtt_server, mqtt_port);
    mqttClient->setCallback([this](char *topic, uint8_t *payload, unsigned int length)
                            { this->onMqttMessage(topic, payload, length); });

    // Activer TLS
    wifiClient->setInsecure(); // Ne pas valider le certificat (à utiliser avec précaution)
    // OU
    // wifiClient.setCACert(ca_cert); // Utiliser un certificat CA personnalisé

    // Se connecter au broker MQTT
    connectToMqtt();
    delay(10);
    myStone->setView("w_dashbord");
    myStone->setView("overlay_layout");
    displayModal("Installation correct", "Tous les modules et composants sont operationnels.", "Il est temps de peser!");
    return true;
}

void PowderScaleController::connectToWiFi()
{
    Serial.println("Connecting to Wi-Fi...");

    String savedSSID = readString(SSID_OFFSET);
    String savedPassword = readString(WIFI_PASSWORD_OFFSET);

    const char *ssidUsed = (isStringValid(savedSSID)) ? savedSSID.c_str() : ssid;
    const char *passwordUsed = (isStringValid(savedPassword)) ? savedPassword.c_str() : password;

    WiFi.begin(ssidUsed, passwordUsed);

    while (WiFi.status() != WL_CONNECTED)
    {
        delay(1000);
        Serial.println("Connecting to WiFi...");
    }
    Serial.println("Connected to Wi-Fi");
}

void PowderScaleController::connectToMqtt()
{
    Serial.println("Connecting to MQTT...");
    while (!mqttClient->connected())
    {
        if (mqttClient->connect(
                "ESP32Client",            // Client ID
                mqtt_user,                // Username
                mqtt_password,            // Password
                "/scale/1/status/online", // Topic LWT
                0,                        // QoS LWT
                true,                     // Retain LWT
                "{\"online\":false}"      // Message LWT
                ))
        {
            Serial.println("Connected to MQTT");
            mqttClient->setCallback([this](char *topic, byte *payload, unsigned int length)
                                    { handleCommand(topic, payload, length); });

            // Abonnements
            mqttClient->subscribe("/scale/1/status/details");
            mqttClient->subscribe("/scale/1/status/online");
            mqttClient->subscribe("/scale/1/status/calibration/auto");
            mqttClient->subscribe("/scale/1/status/calibration/manual"); // Correction: "manual" au lieu de "manuel"
            mqttClient->subscribe("/scale/1/commands");

            // Publication de l'état online
            mqttClient->publish("/scale/1/status/online", "{\"online\":true}", true);
        }
        else
        {
            Serial.print("Failed, rc=");
            Serial.print(mqttClient->state());
            Serial.println(" Retrying in 5 seconds...");
            delay(5000);
        }
    }
}

void PowderScaleController::onMqttMessage(char *topic, uint8_t *payload, unsigned int length)
{
    Serial.println("Message reçu :");
    Serial.println(topic);
    for (int i = 0; i < length; i++)
    {
        Serial.print((char)payload[i]);
    }
    Serial.println();
}

void PowderScaleController::lookingForDatas()
{
    datasRead dr = myStone->getValidsDatasIfExists();
    if (dr.data[0] != '\0')
    {
        Serial.println("Data:");
        Serial.println(dr.data);
        switch (dr.id)
        {
        case 0x1001: // Bouton
        {
            std::string buttonName = dr.data;
            buttonPressed(buttonName);
            break;
        }
        }
    }
};

/**
 * buttonPressed(std::string buttonName)
 *
 * @param buttonName de type std::string "Nom du bouton"
 *
 * Génère la conséquence à la suite de la pression d'un de ces boutons
 */
void PowderScaleController::buttonPressed(std::string buttonName)
{
    buttonName.c_str();

    if (buttonName == "btn_rtn_to_dashbord_wm" || buttonName == "btn_validate_wm")
    {
        isMeasuring = false;
    }

    if (buttonName == "btn_auto_calibrage") // Prochaine maj: Ne pas hardcoder
    {
        handleAutoCalibration();
    }
};

void PowderScaleController::loop()
{
    lookingForDatas();
    // printAccelerometerData();
    if (isMoving())
    {
        displayModal("Avertissement", "Pour une meilleure precision et ne rien briser", "Il est recommande de ne pas deplacer la balance lors de son usage.");
    }

    if (!mqttClient->connected())
    {
        connectToMqtt();
    }
    mqttClient->loop();

    if (scaleClass->is_ready())
    {
        if (!weightSensorFunctionnal)
            weightSensorFunctionnal = true;
        if (isMeasuring)
        {
            float weight = scaleClass->get_units_kg(20);
            if (weight < 0)
            {
                weight = 0;
            }

            if (abs(weight - lastSentWeight) > WEIGHT_THRESHOLD)
            {
                myStone->setTextLabel("lbl_weight", floatToChar(weight));
                lastSentWeight = weight;
                publishStatus(true);
            }
        }
        erreurCount = 0;
    }
    else
    {
        if (isMeasuring)
        {
            erreurCount++;
        }

        if (erreurCount > 2)
        {
            displayModal("Erreur capteur de poids", "Les capteurs de poids ne repondent pas.", "Impossible de recuperer la masse.");
            weightSensorFunctionnal = false;
        }
    }
    publishStatus(firstInit);
    if (firstInit)
    {
        firstInit = false;
    }
    
}

void PowderScaleController::displayModal(const char *title, const char *desc1, const char *desc2)
{
    myStone->setView("pup_alerte");
    myStone->setTextLabel("lbl_title_alert_pup", title);
    myStone->setTextLabel("lbl_description_alert_pup", desc1);
    myStone->setTextLabel("lbl_description2_alert_pup", desc2);
}

bool PowderScaleController::beginAccelerometer()
{

    if (accelerometer == nullptr)
    {
        Serial.println("ERREUR: Pointeur accelerometer non initialisé!");
        accelerometerInit = false;
        accelerometerFunctional = false;
        return accelerometerInit;
    }

    Wire.begin(SDA_PIN, SCL_PIN);
    accelerometer->setWire(&Wire);
    accelerometer->beginAccel();
    if (accelerometer->accelUpdate() != 0)
    {
        Serial.println("Échec de la lecture initiale de l'accéléromètre");
        accelerometerInit = false;
        accelerometerFunctional = false;
        return accelerometerInit;
    }

    accelerometerInit = true;
    accelerometerFunctional = true;
    return accelerometerInit;
}

bool PowderScaleController::isMoving()
{
    if (!accelerometerInit)
    {
        if (!beginAccelerometer())
        {
            Serial.println("Avertissement: Accéléromètre non fonctionnel");
            return false;
        }
    }

    if (accelerometer->accelUpdate() != 0)
    {
        Serial.println("Erreur de lecture");
        accelerometerFunctional = false;
        return false;
    }

    float currentX = accelerometer->accelX();
    float currentY = accelerometer->accelY();
    float currentZ = accelerometer->accelZ();
    float currentNorm = sqrt(currentX * currentX + currentY * currentY + currentZ * currentZ);
    lastNorm = currentNorm;

    if (abs(currentNorm - ACCEL_NORM_STABLE) > 0.5f)
    { // Seuil élargi
        Serial.println("Données aberrantes");
        accelerometerFunctional = false;
        return false;
    }
    if (!isAccInitialized)
    {
        lastAccelX = currentX;
        lastAccelY = currentY;
        lastAccelZ = currentZ;
        isAccInitialized = true;
    }
    float deltaX = abs(currentX - lastAccelX);
    float deltaY = abs(currentY - lastAccelY);
    float deltaZ = abs(currentZ - lastAccelZ);

    lastAccelX = currentX;
    lastAccelY = currentY;
    lastAccelZ = currentZ;

    bool isMoving = (deltaX > MOVEMENT_THRESHOLD) ||
                    (deltaY > MOVEMENT_THRESHOLD) ||
                    (deltaZ > MOVEMENT_THRESHOLD);

    // Debug (à commenter en production)
    // Serial.printf("Deltas: X=%.3f Y=%.3f Z=%.3f\n", deltaX, deltaY, deltaZ);
    // Serial.printf("Seuil: %.3f | Moving: %d\n", MOVEMENT_THRESHOLD, isMoving);

    accelerometerFunctional = true;
    isAccMoving = isMoving;
    return isMoving;
}

void PowderScaleController::printAccelerometerData()
{
    if (accelerometer->accelUpdate() == 0)
    {
        Serial.print("X: ");
        Serial.print(accelerometer->accelX());
        Serial.print(" Y: ");
        Serial.print(accelerometer->accelY());
        Serial.print(" Z: ");
        Serial.print(accelerometer->accelZ());
        Serial.print(" Norm: ");
        Serial.println(sqrt(pow(accelerometer->accelX(), 2) +
                            pow(accelerometer->accelY(), 2) +
                            pow(accelerometer->accelZ(), 2)));
    }
}

void PowderScaleController::publishStatus(bool forced)
{
    static unsigned long lastPublishTime = 0;
    const unsigned long publishInterval = 10000; // 1000 secondes

    unsigned long currentTime = millis();
    if (!forced && (currentTime - lastPublishTime < publishInterval))
    {
        return;
    }
    lastPublishTime = currentTime;

    int accelerometerStatus = 0;

    if (isAccMoving)
    {
        accelerometerStatus = 1;
    }
    else
    {
        accelerometerStatus = accelerometerFunctional ? 2 : 0;
    }
    int weightSensorStatus = 0;

    if (weightSensorFunctionnal)
    {
        weightSensorStatus = 2;
    }

    // Créer le JSON
    DynamicJsonDocument doc(512);
    doc["accelerometerStatus"] = accelerometerStatus;
    doc["accX"] = floatToChar(lastAccelX);
    doc["accY"] = floatToChar(lastAccelY);
    doc["accZ"] = floatToChar(lastAccelZ);
    doc["accNorm"] = floatToChar(lastNorm);
    doc["weightSensorStatus"] = weightSensorStatus;
    doc["weightDetected"] = floatToChar(lastSentWeight);
    doc["calibrationIndex"] = scale;
    doc["display"] = 2;
    doc["datetimeDelivery"] = getCurrentTimestamp();

    char jsonBuffer[512];
    serializeJson(doc, jsonBuffer);
    mqttClient->publish("/scale/1/status/details", jsonBuffer, true);
}

void PowderScaleController::handleCommand(const char *topic, byte *payload, unsigned int length)
{
    String message;
    for (unsigned int i = 0; i < length; i++)
    {
        message += (char)payload[i];
    }

    DynamicJsonDocument doc(256);
    DeserializationError error = deserializeJson(doc, message);

    if (error)
    {
        Serial.print("Erreur JSON: ");
        Serial.println(error.c_str());
        return;
    }

    if (strcmp(topic, "/scale/1/commands") == 0)
    {
        const char *command = doc["command"];

        if (strcmp(command, "get_status") == 0)
        {
            publishStatus(true);
        }
        else if (strcmp(command, "calibrate") == 0)
        {
            const char *type = doc["type"];
            if (strcmp(type, "auto") == 0)
            {
                handleAutoCalibration();
            }
            // else if (strcmp(type, "manual") == 0) {
            //     float index = doc["index"] | 0.0f;
            //     handleManualCalibration(index);
            // }
        }
    }
}

void PowderScaleController::handleAutoCalibration()
{
    // Implémentez la logique de calibration auto ici
    scaleClass->tare();

    DynamicJsonDocument doc(128);
    doc["datetime"] = getCurrentTimestamp();

    char jsonBuffer[128];
    serializeJson(doc, jsonBuffer);
    mqttClient->publish("/scale/1/status/calibration/auto", jsonBuffer);
    if (!isMeasuring)
    {
        myStone->setView("w_weight_measure");
    }
    isMeasuring = true;
    displayModal("Calibrage", "La balance est calibree", "Remise a zero : OK");
}

// void PowderScaleController::handleManualCalibration(float index) {
//     scaleClass->set_scale(index);

//     // Publier le résultat
//     DynamicJsonDocument doc(128);
//     doc["datetime"] = getCurrentTimestamp();
//     doc["calibrationIndex"] = index;

//     char jsonBuffer[128];
//     serializeJson(doc, jsonBuffer);
//     mqttClient->publish("/scale/1/status/calibration/manual", jsonBuffer);
// }

long PowderScaleController::getCurrentTimestamp()
{
    if (!timeClient.update())
    {
        timeClient.forceUpdate();
    }
    return timeClient.getEpochTime();
}

void PowderScaleController::initNTP()
{
    Serial.println("Initialisation NTP...");
    timeClient.begin();
    Serial.println("NTP begin done");

    // Attendre la première mise à jour
    int retries = 0;
    while (!timeClient.update() && retries < 5)
    {
        Serial.println("Mise à jour NTP...");
        timeClient.forceUpdate();
        delay(500);
        retries++;
    }

    if (retries >= 5)
    {
        Serial.println("Échec de la synchronisation NTP");
    }
    else
    {
        Serial.println("Heure synchronisée: " + String(timeClient.getFormattedTime()));
    }
}