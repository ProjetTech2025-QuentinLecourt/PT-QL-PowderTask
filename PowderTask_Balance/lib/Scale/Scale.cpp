/**
 * Gestion de la balance
 *
 * @file Scale.cpp
 * @author Lecourt Quentin
 * @brief Methode de gestion de la balance
 * @version 1.0
 * @date Création : 14/03/2025
 * @date Dernière mise à jour : Création : 14/03/2025
 */
#include "Scale.h"

// Constructor
Scale::Scale(int dt = 33, int sck = 25) {
    _dt = dt;
    _sck = sck;
    _scale = DEFAULT_SCALE;
    _tare = DEFAULT_TARE;
    _ready = false;
    
}

// Destructor
Scale::~Scale() {
    if (scaleHX711 != nullptr) {
        delete scaleHX711;
        scaleHX711 = nullptr;
    }
}

// Method to initialize the scale
bool Scale::init(int _loop = 300, int _delay = 100) {
    scaleHX711 = new HX711();
    delay(_delay);
    scaleHX711->begin(_dt, _sck);
    delay(_delay);
    int i = 0;
    int loop = _loop;
    while (!scaleHX711->is_ready() && i < loop) {
        scaleHX711->wait_ready(_delay*2);
        i++;
    }
    if (scaleHX711 == nullptr || i == loop-1 || !scaleHX711->is_ready()) {
        return false;
    }
    scaleHX711->set_scale(_scale);
    delay(_delay*10);
    scaleHX711->tare(_tare);
    return true;
}

void Scale::set_scale(float _scale){
    _scale = _scale;
    scaleHX711->set_scale(_scale);
}
void Scale::set_tare(byte tare){
    _tare = tare;
}
void Scale::tare(){
    scaleHX711->tare(_tare);
}

bool Scale::is_ready(){
    return scaleHX711->is_ready();
}

long Scale::read(){
    return scaleHX711->read();
}
float Scale::get_units_g(int times){
    read();
    return scaleHX711->get_units(times);
}

float Scale::get_units_kg(int times){
    read();
    float value = scaleHX711->get_units(times);
    value = value / 1000;
    return value;
}