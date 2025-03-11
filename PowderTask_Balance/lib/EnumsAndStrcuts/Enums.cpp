#ifndef ENUMS_CPP
#define ENUMS_CPP

// Enumération des langues disponibles
enum LANGUAGE {
    FRENCH,
    ENGLISH,
    GERMAN,
    LANGUAGES_NUMBER
};

// Enumération des clés 
enum KEY {
    LBL_WEIGHT
};

// Enumération des unités de température
enum UNITY_WEIGHT
{
    UNITY_LB,
    UNITY_KG
};

enum PAGE
{
    // Loading
    HOME_PAGE,
    //Principal
    W_DASBOARD,
    W_WEIGHT_MEASURE,
    // Paramters
    W_PARAMS,
    W_PARAM_MANUAL_CALIBRATION,
    W_PARAM_UNIT,
    W_PARAM_LANGUAGE,
    W_PARAM_WIFI,
    W_PARAM_LOGIN,
    //Other
    PUP_ALERTE,
    PUP_MORE_ABOUT_MC,
    DIAG_MC
};
#endif