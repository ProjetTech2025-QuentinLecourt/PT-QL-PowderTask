#include <Arduino.h>
#include "HX711.h"
#include "MyStone.h"

// Définir les broches pour HX711
#define DT 19  // Broche DATA
#define SCK 05 // Broche CLOCK

#define TX 17
#define RX 16

HX711 scale; // Instance du module HX711

MyStone *myStone = nullptr;
// Instanciation de la classe MyStone

char* floatToChar(float theFloatValue)
{
    char *valeurSTR = new char[7];
    sprintf(valeurSTR, "%.1f", theFloatValue);
    return valeurSTR;
};

void lookingForDatas()
{
    datasRead dr = myStone->getValidsDatasIfExists();
    Serial.println("Data:");
    Serial.println(dr.data);
};


void setup()
{
  Serial.begin(9600); // Initialiser la communication série

  myStone = new MyStone(115200, SERIAL_8N1, RX,TX);
  Serial.println("Initialisation du HX711...");
  delay(1000);
  myStone->changePage("w_weight_measure");

  // Configurer les broches DATA et CLOCK
  scale.begin(DT, SCK);
  scale.set_gain(); // Configurer le gain du module

  // Attendre que le module soit prêt
  // Faire une méthode dans la classe MyCells pour attendre que le module soit prêt ou que X temps soit passé.
  scale.wait_ready(200);
  // Vérification si le module est prêt
  if (!scale.is_ready())
  {
    Serial.println("Erreur : HX711 non connecté !");
    while (1)
      ;
  }

  // Configurer le facteur d'échelle (calibration)
  scale.set_scale(21.7074f); // Facteur à ajuster en fonction de votre capteur
  delay(3000);             // Laissez le HX711 se stabiliser
  scale.tare();            // Ajustez la tare       // Réinitialiser le poids à zéro

  Serial.println("HX711 prêt !");
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
  myStone->changePage("w_weight_measure");
  if (scale.is_ready())
  {
    long raw = scale.read();            // Lire la valeur brute de l'ADC
    float weight = scale.get_units(10); // Moyenne de 10 lectures

    // Serial.print("Poids : ");
    // Serial.print(weight);
    // Serial.println(" g");
    // myStone->setTextLabel("lbl_weight",floatToChar(weight));

    // Serial.print("Valeur brute : ");
    // Serial.println(scale.get_units(1));
    //delay(500);
  }
  else
  {
    Serial.println("Erreur : Impossible de lire les données du HX711 !");
  }

  delay(1000);
}