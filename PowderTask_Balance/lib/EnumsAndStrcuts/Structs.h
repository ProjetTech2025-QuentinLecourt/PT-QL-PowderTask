
#ifndef STRUCTS_H
#define STRUCTS_H
// Structure local utilisés pour garder les informations lues de l'écran

struct datasRead
{
    int id; // Si 0 alors il n'y a pas de données, <0 Numero commande non traitée, >1 Numero de la commande traité
    char command[80];
    char data[80];
    char nameEdt[50];
    char valueEdt[50];
    int keyValue;
    char line[2048];
};

struct controllerInit {
    // Scale GPIO
    int dt;
    int sck;
    // Scale params
    float scale;
    byte tare;
    // Stone
    int stoneSpeed;
    int rx;
    int tx;
    // Wifi
    char* ssid;
    char* password;
    // MQTT
    const char* mqtt_server;
    int mqtt_port;
    const char* mqtt_user;
    const char* mqtt_password;
};
#endif