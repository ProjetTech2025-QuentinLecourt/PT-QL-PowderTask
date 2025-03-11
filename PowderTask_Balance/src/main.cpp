#include <Arduino.h>
#include "HX711.h"

// Définir les broches pour HX711
#define DT 19  // Broche DATA
#define SCK 05 // Broche CLOCK

HX711 scale; // Instance du module HX711

void setup()
{
  Serial.begin(9600); // Initialiser la communication série
  Serial.println("Initialisation du HX711...");

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
  if (scale.is_ready())
  {
    long raw = scale.read();            // Lire la valeur brute de l'ADC
    float weight = scale.get_units(10); // Moyenne de 10 lectures

    Serial.print("Poids : ");
    Serial.print(weight);
    Serial.println(" g");

    Serial.print("Valeur brute : ");
    Serial.println(scale.get_units(1));
    delay(500);
  }
  else
  {
    Serial.println("Erreur : Impossible de lire les données du HX711 !");
  }

  delay(1000);
}