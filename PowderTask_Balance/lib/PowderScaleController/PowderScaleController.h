#ifndef POWDERSCALECONTROLLER_H
#define POWDERSCALECONTROLLER_H

#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>
#include "Scale.h"
#include "MyStone.h"
#include "Convertor.h"
#include "Structs.h"

class PowderScaleController
{
private:
    int rxGpio;
    int txGpio;
    int stoneSpeed;

    int sclale_dt;
    int scale_sck;

    int scale;
    byte tare;

    char *ssid;
    char *password;

    // MQTT
    const char *mqtt_server;
    int mqtt_port = 8883;
    const char *mqtt_user;
    const char *mqtt_password;

    Scale *scaleClass = nullptr;
    MyStone *myStone = nullptr;
    WiFiClientSecure *wifiClient = nullptr;
    PubSubClient *mqttClient = nullptr;

public:
    PowderScaleController(controllerInit init);
    ~PowderScaleController();
    bool init();
    void connectToWiFi();
    void connectToMqtt();
    void onMqttMessage(char *topic, byte *payload, unsigned int length);
    void lookingForDatas();
    void loop();
};

#endif