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

bool PowderScaleController::init()
{
    if (myStone == nullptr)
    {
        return false;
    }
    // Verification de la communication avec le HX711
    if (scaleClass == nullptr)
    {
        myStone->laodView("pup_alerte");
        myStone->setTextLabel("lbl_title_alert_pup", "Erreur d'instanciation");
        myStone->setTextLabel("lbl_description_alert_pup", "Impossible d'instancier la classe de la balance !");
        // myStone->setTextLabel("lbl_description2_alert_pup", "Erreur !");
        return false;
    }
    if (!scaleClass->init(400, 100))
    {
        myStone->laodView("pup_alerte");
        myStone->setTextLabel("lbl_title_alert_pup", "Erreur d'initialisation");
        myStone->setTextLabel("lbl_description_alert_pup", "Impossible d'intialiser la balance !");
        // myStone->setTextLabel("lbl_description2_alert_pup", "Erreur !");
        return false;
    }
    // Suppression de la classe Scale suite à la verification
    // delete scaleClass;

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
    delay(100);

    scaleClass->set_scale(scale);
    scaleClass->tare(); // Réinitialiser le poids à zéro
    delay(1000);
    myStone->laodView("w_dashbord");
    delay(1000);
    myStone->laodView("overlay_layout");
    return true;
}

void PowderScaleController::connectToWiFi()
{
    Serial.println("Connecting to Wi-Fi...");
    
    WiFi.begin(ssid, password);

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
  if (!mqttClient->connected()) {
    connectToMqtt();
  }
  mqttClient->loop();
  
  if (scaleClass->is_ready())
  {            // Lire la valeur brute de l'ADC
    float weight = scaleClass->get_units_kg(30);

    Serial.print("Poids : ");
    Serial.print(weight);
    Serial.println(" Kg");
    myStone->setTextLabel("lbl_weight",floatToChar(weight));
    mqttClient->publish("test",floatToChar(weight));
    Serial.print("Valeur brute : ");
    Serial.println(scaleClass->get_units_g(1));
    delay(1000);
  }
  else
  {
    Serial.println("Erreur : Impossible de lire les données du HX711 !");
  }

  delay(100);
}