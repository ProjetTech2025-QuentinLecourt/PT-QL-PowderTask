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
#include <EEPROM.h>

#define EEPROM_SIZE 512  // Taille max ESP32 (4 Ko possible)

// Offsets (plage d'octect pour chaque données)
#define EMAIL_OFFSET        0
#define PASSWORD_OFFSET     64
#define JWT_OFFSET          128
#define SSID_OFFSET         384
#define WIFI_PASSWORD_OFFSET 416
#define LANGUAGE_OFFSET      480
#define WEIGHT_TYPE_OFFSET   481


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

    void initEEPROM();
    bool isStringValid(const String &str);

    void saveString(int offset, const String &str);
    String readString(int offset);

    void saveEnum(int offset, uint8_t value);
    uint8_t readEnum(int offset);

    void clearEEPROM();

public:
    PowderScaleController(controllerInit init);
    ~PowderScaleController();
    bool init();
    void connectToWiFi();
    void connectToMqtt();
    void onMqttMessage(char *topic, byte *payload, unsigned int length);
    void lookingForDatas();
    void loop();

    bool setupScaleBalance();
    void makeNewMeasure();
    void makeFinalMeasure();
};

#endif