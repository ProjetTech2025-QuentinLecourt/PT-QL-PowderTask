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
    delete scale;
}

// Method to initialize the scale
bool Scale::init(int _loop = 300, int _delay = 100) {
    scale = new HX711();
    delay(_delay);
    scale->begin(_dt, _sck);
    delay(_delay);
    int i = 0;
    int loop = _loop;
    while (!scale->is_ready() && i < loop) {
        scale->wait_ready(_delay*2);
        i++;
    }
    if (scale == nullptr || i == loop-1 || !scale->is_ready()) {
        return false;
    }
    scale->set_scale(_scale);
    delay(_delay*10);
    scale->tare(_tare);
    return true;
}

void Scale::set_scale(float _scale){
    _scale = _scale;
    scale->set_scale(_scale);
}
void Scale::set_tare(byte tare){
    _tare = tare;
}
void Scale::tare(){
    scale->tare(_tare);
}

bool Scale::is_ready(){
    return scale->is_ready();
}

long Scale::read(){
    return scale->read();
}
float Scale::get_units_g(int times){
    read();
    return scale->get_units(times);
}

float Scale::get_units_kg(int times){
    read();
    float value = scale->get_units(times);
    value = value / 1000;
    return value;
}