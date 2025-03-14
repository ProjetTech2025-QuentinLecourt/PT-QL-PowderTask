/**
 * Classe MyStone
 * Des méthodes supplémentaires sont ajoutées
 * @file MyStone.h
 * @author Lecourt Quentin
 * @cite Depuis la base de MyStone de Alain Dubé
 * @brief Declaration de la classe MyStone
 * @version 1.4
 * @date Création : 01/10/2024
 * @date Dernière mise à jour : 26/11/2024
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
    // Capsule de reception
    static const char *COMMAND_HEADER;
    static const char *COMMAND_TAIL;
    // Capsule d'envoie
    static const char *COMMAND_BEGIN;
    static const char *COMMAND_END;
    // Les commandes
    static const char *COMMAND_SET_TEXT;
    static const char *COMMAND_SET_VALUE;
    static const char *COMMAND_GET_TEXT;
    static const char *COMMAND_OPEN_WIN;
    static const char *COMMAND_BACK_WIN;
    // Les types de windgets
    static const char *COMMAND_TYPE; // Pour designer le type
    static const char *COMMAND_EDIT;
    static const char *COMMAND_BUTTON;
    static const char *COMMAND_LABEL;
    static const char *COMMAND_TEXT;
    static const char *COMMAND_RADIO_BUTTON;
    // Autres
    static const char *COMMAND_WINDGET;
    static const char *COMMAND_VALUE;
    static const char *COMMAND_QUOTE;

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

    void changePage(const char *pageName);
};
#endif
