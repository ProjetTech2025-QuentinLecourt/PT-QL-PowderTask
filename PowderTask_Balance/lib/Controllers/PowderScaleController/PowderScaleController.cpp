#include "PowderScaleController.h"

PowderScaleController::PowderScaleController(controllerInit init)
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

    if (!beginAccelerometer())
    {
        displayModal("Erreur d'initialisation", "L'accelerometre ne repond pas !", "Impossible de s'en servir");
        return false;
    }

    EEPROM.begin(EEPROM_SIZE);
    (readEnum(INSTALLATION_OFFSET) == NOT_INSTALL) ? initEEPROM(true) : initEEPROM(false);

    // Se connecter au Wi-Fi
    connectToWiFi();

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
        if (mqttClient->connect("ESP32Client", mqtt_user, mqtt_password))
        {
            Serial.println("Connected to MQTT");
            mqttClient->setCallback([this](char *topic, byte *payload, unsigned int length)
                                    { handleCommand(topic, payload, length); });
            mqttClient->subscribe("/scale/1/status");
            mqttClient->subscribe("/scale/1/commands");
            mqttClient->subscribe("test");
            mqttClient->publish("test", "Nouvelle connexion");
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
        if (!weightSensorFunctionnal) weightSensorFunctionnal = true;
        float weight = scaleClass->get_units_kg(20);
        if (weight < 0)
        {
            weight = 0;
        }

        if (abs(weight - lastSentWeight) > WEIGHT_THRESHOLD)
        {
            myStone->setTextLabel("lbl_weight", floatToChar(weight));
            mqttClient->publish("test", floatToChar(weight));
            lastSentWeight = weight;
        }
    }
    else
    {
        displayModal("Erreur capteur de poids", "Les capteurs de poids ne répondent pas.", "Impossible de récupérer la masse.");
        weightSensorFunctionnal = false;
    }
    
    publishStatus(false);
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

    if (doc["command"] == "get_status")
    {

        // String fullTopic = String(topic);
        // int idStart = fullTopic.indexOf("/scale/") + 7;
        // int idEnd = fullTopic.indexOf("/commands");
        // String scaleId = fullTopic.substring(idStart, idEnd);

        publishStatus(true);
    }
}

void PowderScaleController::publishStatus(bool forced = false)
{
    if ((lastAccelerometerFunctional != accelerometerFunctional) || (lastWeightSensorFunctionnal != weightSensorFunctionnal) || forced)
    {
        lastAccelerometerFunctional = accelerometerFunctional;
        lastWeightSensorFunctionnal = weightSensorFunctionnal;
        DynamicJsonDocument doc(256);

        // Ajouter les données au JSON
        doc["accelerometer"] = lastAccelerometerFunctional;
        doc["weightSensor"] = lastWeightSensorFunctionnal;
        doc["timestamp"] = millis();

        // Sérialiser le JSON
        char jsonBuffer[256];
        serializeJson(doc, jsonBuffer);

        // Construire le topic de réponse
        char statusTopic[50];
        snprintf(statusTopic, sizeof(statusTopic), "/scale/%s/status", scaleId);

        // Publier le message
        mqttClient->publish(statusTopic, jsonBuffer);
    }
}