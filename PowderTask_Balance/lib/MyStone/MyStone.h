/**
 * Classe MyStone
 * Des méthodes supplémentaires sont ajoutées
 * @file MyStone.h
 * @author Lecourt Quentin
 * @brief Declaration de la classe MyStone
 * @version 2.0.1
 * @date Création : 01/10/2024
 * @date Dernière mise à jour : 27/03/2025
 */
#ifndef MYSTONE_H
#define MYSTONE_H

#include <string>
#include "MySerial.h"
#include "Structs.h"

class MyStone
{
private:
    MySerial *mySerial = NULL;

    bool findHeader();
    unsigned short getCMDorLEN();
    bool readTail();

    // Element de structure d'un message UART avec le Stone
    /*----------------- CAPSULE -----------------*/
    static const char *CMD_HEADER;
    static const char *CMD_TAIL;
    // Capsule d'envoie
    static const char *CMD_BEGIN;
    static const char *CMD_END;

    /*----------------- COMMAND CODE -----------------*/
    static const char *CMD_SET_VISIBLE;
    static const char *CMD_SET_ENABLE;
    static const char *CMD_SET_TEXT;
    static const char *CMD_SET_VALUE;
    static const char *CMD_GET_TEXT;
    static const char *CMD_OPEN_WIN;
    static const char *CMD_BACK_WIN;
    static const char *CMD_SET_IMAGE;

    /*----------------- WIDGET TYPE -----------------*/
    static const char *CMD_TYPE; // Pour designer le type
    static const char *CMD_EDIT;
    static const char *CMD_BUTTON;
    static const char *CMD_LABEL;
    static const char *CMD_TEXT;
    static const char *CMD_RADIO_BUTTON;
    static const char *CMD_WIDGET_TYPE;
    static const char *CMD_IMAGE;

    /*----------------- KEYS & VALUES-----------------*/
    static const char *CMD_WIDGET_KEY;
    static const char *CMD_VALUE;
    static const char *CMD_ENABLE;
    static const char *CMD_VISIBLE;
    static const char *CMD_IMAGE_KEY;

    /*----------------- OTHERS -----------------*/
    static const char *CMD_QUOTE;

    String generateCmd(const char *cmdCode, const char *type, const char *widget, const char *key, const char *value);

public:
    MyStone(int speed, uint32_t config, int rxd, int txd);
    ~MyStone();

    datasRead getValidsDatasIfExists();
    // Ordres aux Stone
    void getEditTextValue(const char *editName);

    void setTextLabel(const char *labelName, const char *text);
    void setTextButton(const char *buttonName, const char *text);
    void setTipsEdit(const char *editName, const char *tips);
    void setRadioButtonTrue(const char *radioButtonName);
    void setImage(const char* widgetName,const char* imageName);

    void setView(const char *pageName);

    void setEnable(const char *widgetName, bool enable);
    void setVisible(const char *widgetName, bool enable);
};
#endif
