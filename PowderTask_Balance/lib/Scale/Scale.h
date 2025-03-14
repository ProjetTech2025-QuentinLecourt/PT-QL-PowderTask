/**
 * Classe Scale
 * @file Scale.h
 * @author Lecourt Quentin
 * @ref HX711.h
 * @brief Declaration des méthodes de gestion de la balance
 * @version 1.0
 * @date Création : 14/03/2025
 * @date Dernière mise à jour : 14/03/2025
 */
#ifndef SCALE_H
#define SCALE_H

#include <HX711.h>

class Scale
{
    private:
        const float DEFAULT_SCALE = 21.7074f;
        const byte DEFAULT_TARE = 20;

        HX711 *scale = nullptr;
        int _dt;
        int _sck;
        float _scale;
        byte _tare;
        bool _ready;

        long read();

    public:
        Scale(int dt, int sck);
        ~Scale();
        bool init(int loop, int delay);
        void set_scale(float scale);
        void set_tare(byte tare);
        void tare();
        bool is_ready();
        float get_units_g(int times);
        float get_units_kg(int times);

};

#endif