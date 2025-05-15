#ifndef POWDERSCALECONTROLLER_H
#define POWDERSCALECONTROLLER_H

#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>
#include "../../Devices/Scale/Scale.h"
#include "../../Devices/MyStone/MyStone.h"
#include "../../Core/Useful/Convertor.h"
#include "../../Core/EnumsAndStrcuts/Structs.h"
#include "../../Core/EnumsAndStrcuts/Enums.h"
#include <EEPROM.h>
#include <Wire.h>
#include <MPU9250_asukiaaa.h>
#include <ArduinoJson.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

#define EEPROM_SIZE 512  // Taille max ESP32 (4 Ko possible)

// Offsets (plage d'octect pour chaque données)
#define EMAIL_OFFSET            0
#define LOGIN_PASSWORD_OFFSET   64
#define JWT_OFFSET              128
#define SSID_OFFSET             384
#define WIFI_PASSWORD_OFFSET    416
#define LANGUAGE_OFFSET         480
#define WEIGHT_UNITE_OFFSET     481
#define INSTALLATION_OFFSET     482

enum INSTALL_STATE {
    NOT_INSTALL,
    INSTALL
};

#define SDA_PIN 15
#define SCL_PIN 04
#define MOVEMENT_THRESHOLD 0.2 // Seuil de mouvement en g
#define ACCEL_NORM_STABLE 1.0  // Valeur attendue de la norme au repos (~1g)

#define WEIGHT_THRESHOLD 0.01 



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

    // Accelerometre
    bool isAccInitialized = false;
    float lastAccelX = 0;
    float lastAccelY = 0;
    float lastAccelZ = 0;
    float lastNorm   = 0;
    float movementThreshold;
    unsigned long lastCheckTime;

    float lastSentWeight = 0;

    const char* scaleId = "1";

    // Variables d'état 
    bool firstInit = true;
    bool accelerometerInit = false;
    bool accelerometerFunctional = false;
    bool weightSensorFunctionnal = false;
    unsigned short erreurCount = 0;
    bool isMeasuring = false;
    bool isAccMoving = false;
    bool lastAccelerometerFunctional = accelerometerFunctional;
    bool lastWeightSensorFunctionnal = weightSensorFunctionnal;

    // MQTT
    const char *mqtt_server;
    int mqtt_port = 8883;
    const char *mqtt_user;
    const char *mqtt_password;

    WiFiUDP ntpUDP;
    NTPClient timeClient;
    Scale *scaleClass = nullptr;
    MyStone *myStone = nullptr;
    WiFiClientSecure *wifiClient = nullptr;
    PubSubClient *mqttClient = nullptr;
    MPU9250_asukiaaa *accelerometer = nullptr;

    void initEEPROM(bool forced);
    bool isStringValid(const String &str);
    void saveString(int offset, const String &str);
    String readString(int offset);
    void saveEnum(int offset, uint8_t value);
    uint8_t readEnum(int offset);
    void clearEEPROM();

    void displayModal(const char* title, const char* desc1, const char* desc2);
    void publishStatus(bool forced);

    void handleCommand(const char *topic, byte *payload, unsigned int length);
    void handleAutoCalibration();
    // void handleManualCalibration(float index);
    long getCurrentTimestamp();
    void initNTP();
    void buttonPressed(std::string buttonName);

public:
    PowderScaleController(controllerInit init);
    ~PowderScaleController();

    bool beginAccelerometer();
    bool isMoving();
    void printAccelerometerData(); // Affichage des données (débogage)

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