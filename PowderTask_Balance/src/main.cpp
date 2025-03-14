#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>
#include "Scale.h"
#include "MyStone.h"

// Définir les broches pour HX711
#define DT 33  // Broche DATA
#define SCK 25 // Broche CLOCK

#define TX 17
#define RX 16

#define STONE_SPEED 115200

#define ESP_SPEED 9600

Scale* scale = nullptr; // Instance du module HX711

MyStone *myStone = nullptr;
// Instanciation de la classe MyStone

char* floatToChar(float theFloatValue)
{
    char *valeurSTR = new char[7];
    sprintf(valeurSTR, "%.2f", theFloatValue);
    return valeurSTR;
};

void lookingForDatas()
{
    datasRead dr = myStone->getValidsDatasIfExists();
    Serial.println("Data:");
    Serial.println(dr.data);
};

// Remplacez par vos informations de réseau Wi-Fi
const char* ssid = "Q_AP";
const char* password = "1234soleil";

// Remplacez par les informations de votre broker MQTT
const char* mqtt_server = "mqtt.powdertask.quentinlecourt.com";
const int mqtt_port = 8883; // Port standard pour MQTT over TLS
const char* mqtt_user = "pwtk-mqtt";
const char* mqtt_password = "esy6z0%FwS0f*X3vtb5X";

WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient);

void connectToWiFi() {
  Serial.println("Connecting to Wi-Fi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to Wi-Fi");
}

void connectToMqtt() {
  Serial.println("Connecting to MQTT...");
  while (!mqttClient.connected()) {
    if (mqttClient.connect("ESP32Client", mqtt_user, mqtt_password)) {
      Serial.println("Connected to MQTT");
      mqttClient.subscribe("test");
    } else {
      Serial.print("Failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" Retrying in 5 seconds...");
      delay(5000);
    }
  }
}

void onMqttMessage(char* topic, byte* payload, unsigned int length) {
  Serial.println("Message received:");
  Serial.println(topic);
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}


void setup()
{
  Serial.begin(ESP_SPEED); // Initialiser la communication série

  // Se connecter au Wi-Fi
  connectToWiFi();

  // Configurer le client MQTT
  mqttClient.setServer(mqtt_server, mqtt_port);
  mqttClient.setCallback(onMqttMessage);

  // Activer TLS
  wifiClient.setInsecure(); // Ne pas valider le certificat (à utiliser avec précaution)
  // OU
  // wifiClient.setCACert(ca_cert); // Utiliser un certificat CA personnalisé

  // Se connecter au broker MQTT
  connectToMqtt();

  myStone = new MyStone(115200, SERIAL_8N1, RX,TX);
  delay(100);

  scale = new Scale(DT, SCK); // Initialiser le module HX711
  if (scale == nullptr || !scale->init(400, 100))
  {
    myStone->changePage("pup_alerte");
    myStone->setTextLabel("lbl_title_alert_pup", "Erreur d'initialisation");
    myStone->setTextLabel("lbl_description_alert_pup", "Impossible d'intialiser la balance !");
    myStone->setTextLabel("lbl_description2_alert_pup", "Erreur !");
    while (1)
      ;
  }
  else {
    myStone->changePage("pup_alerte");
    myStone->setTextLabel("lbl_title_alert_pup", "OK");
    myStone->setTextLabel("lbl_description_alert_pup", "A !");
    myStone->setTextLabel("lbl_description2_alert_pup", "B !");
    delay(5000);
  }
  // Vérification si le module est prêt
  if (!scale->is_ready())
  {
    myStone->changePage("pup_alerte");
    myStone->setTextLabel("lbl_title_alert_pup", "Erreur de communication");
    myStone->setTextLabel("lbl_description_alert_pup", "Les capteurs de poinds n'arrivent pas à être prêts !");
    myStone->setTextLabel("lbl_description2_alert_pup", "Erreur !");
    while (1)
      ;
  }
  scale->tare();          // Ajustez la tare       // Réinitialiser le poids à zéro
  delay(1000);
  Serial.println("HX711 prêt !");
  myStone->changePage("overlay_layout");
  delay(1000);
  myStone->changePage("w_weight_measure");
}


// float filtered_weight = 0;
// float alpha = 0.1; // Facteur de filtrage (à ajuster)

// void loop() {
//   if (scale.is_ready()) {
//     float weight = scale.get_units(10); // Moyenne de 10 lectures
//     filtered_weight = alpha * weight + (1 - alpha) * filtered_weight;

//     Serial.print("Poids filtré : ");
//     Serial.print(filtered_weight);
//     Serial.println(" g");

//     delay(500);
//   } else {
//     Serial.println("Erreur : Impossible de lire les données du HX711 !");
//   }

//   delay(1000);
// }
void loop()
{
  lookingForDatas();
  if (!mqttClient.connected()) {
    connectToMqtt();
  }
  mqttClient.loop();
  
  if (scale->is_ready())
  {            // Lire la valeur brute de l'ADC
    float weight = scale->get_units_kg(30); // Moyenne de 10 lectures

    Serial.print("Poids : ");
    Serial.print(weight);
    Serial.println(" g");
    myStone->setTextLabel("lbl_weight",floatToChar(weight));
    mqttClient.publish("test",floatToChar(weight));

    Serial.print("Valeur brute : ");
    Serial.println(scale->get_units_g(1));
    delay(500);
  }
  else
  {
    Serial.println("Erreur : Impossible de lire les données du HX711 !");
  }

  delay(10);
}