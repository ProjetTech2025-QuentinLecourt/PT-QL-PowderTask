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
        myStone->setView("pup_alerte");
        myStone->setTextLabel("lbl_title_alert_pup", "Erreur d'instanciation");
        myStone->setTextLabel("lbl_description_alert_pup", "Impossible d'instancier la classe de la balance !");
        // myStone->setTextLabel("lbl_description2_alert_pup", "Erreur !");
        return false;
    }
    if (!scaleClass->init(400, 100))
    {
        myStone->setView("pup_alerte");
        myStone->setTextLabel("lbl_title_alert_pup", "Erreur d'initialisation");
        myStone->setTextLabel("lbl_description_alert_pup", "Impossible d'intialiser la balance !");
        // myStone->setTextLabel("lbl_description2_alert_pup", "Erreur !");
        return false;
    }
    // Suppression de la classe Scale suite à la verification
    // delete scaleClass;
    // scaleClass = nullptr;

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
    Serial.println("Data:");
    Serial.println(dr.data);
};

void PowderScaleController::loop()
{
    lookingForDatas();

    if (!mqttClient->connected())
    {
        connectToMqtt();
    }
    mqttClient->loop();

    if (scaleClass->is_ready())
    { // Lire la valeur brute de l'ADC
        Serial.println("Lecture de la balance...");
        float weight = scaleClass->get_units_kg(20);

        Serial.print("Poids : ");
        Serial.print(weight);
        Serial.println(" Kg");
        myStone->setTextLabel("lbl_weight", floatToChar(weight));
        mqttClient->publish("test", floatToChar(weight));
        Serial.print("Valeur brute : ");
        Serial.println(scaleClass->get_units_g(1));
    }
    else
    {
        Serial.println("Erreur : Impossible de lire les données du HX711 !");
    }
}