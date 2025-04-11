#include <Arduino.h>
#include "../lib/Controllers/PowderScaleController/PowderScaleController.h"
#include "../lib/Core/EnumsAndStrcuts/Structs.h"

#define ESP_SPEED 9600

controllerInit CONTROLLER_INIT = {
    // Scale GPIO
    33,
    25,
    // Scale params
    21.7074f,
    10,
    // Stone
    115200,
    16,
    17,
    // Wifi
    "Q_AP",
    "1234soleil",
    // MQTT
    "mqtt.powdertask.quentinlecourt.com",
    8883,
    "pwtk-mqtt",
    "esy6z0%FwS0f*X3vtb5X"

};

PowderScaleController *powderScaleController = nullptr;

void setup()
{
  Serial.begin(ESP_SPEED); // Initialiser la communication série
  Serial.println("Initialisation du contrôleur de balance...");
  powderScaleController = new PowderScaleController(CONTROLLER_INIT);
  if (powderScaleController == nullptr)
  {
    Serial.println("Erreur : Impossible d'initialiser le contrôleur de balance !");
    while (1)
      ;
  }
  if (!powderScaleController->init())
  {
    Serial.println("Erreur : Impossible d'initialiser l'ensemble contrôleur de balance !");
    while (1)
      ;
  }
}
void loop()
{
  powderScaleController->loop();
  delay(100);
}