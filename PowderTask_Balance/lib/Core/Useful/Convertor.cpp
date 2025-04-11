
#include <Arduino.h>
/**
 * Convertion d'un flottant en chaine de caractères
 * @param theFloatValue la valeur flottante à convertir
 * @return la chaine de caractères
 */
char* floatToChar(float theFloatValue)
{
    char *valeurSTR = new char[7];
    sprintf(valeurSTR, "%.2f", theFloatValue);
    return valeurSTR;
};
