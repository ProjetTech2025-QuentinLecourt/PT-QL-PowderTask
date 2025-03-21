/**
 * Gestion des communications avec l'écran Stone
 *
 * @file MyStone.cpp
 * @author Lecourt Quentin
 * @brief Methode de la classe MyStone
 * @version 2.0
 * @date Création : 01/10/2024
 * @date Dernière mise à jour : 21/03/2025
 */
#include "MyStone.h"

/**
 * Constante static pour la instruction de retour Serie du Stone
 */
/*----------------- CAPSULE -----------------*/
// Frame header
const char *MyStone::CMD_HEADER = "ST<";
// Frame tail
const char *MyStone::CMD_TAIL = ">ET";

// Frame debut d'une commande
const char *MyStone::CMD_BEGIN = "ST<{\"cmd_code\":";
// Frame fin d'une commande
const char *MyStone::CMD_END = "}>ET";

/*----------------- COMMAND CODE -----------------*/
// Frame set_visible
const char *MyStone::CMD_SET_VISIBLE = "\"set_visible\"";
// Frame set_enable
const char *MyStone::CMD_SET_ENABLE = "\"set_enable\"";
// Frame get_value
const char *MyStone::CMD_GET_TEXT = "\"get_text\"";
// Frame set_value
const char *MyStone::CMD_SET_VALUE = "\"set_value\"";
// Frame set_text
const char *MyStone::CMD_SET_TEXT = "\"set_text\"";
// Frame open_win
const char *MyStone::CMD_OPEN_WIN = "\"open_win\"";
// Frame back_win
const char *MyStone::CMD_BACK_WIN = "\"back_win\"";

/*----------------- WIDGET TYPE -----------------*/
// Frame type
const char *MyStone::CMD_TYPE = ",\"type\":";
// Frame edit
const char *MyStone::CMD_EDIT = "\"edit\"";
// Frame button
const char *MyStone::CMD_BUTTON = "\"button\"";
// Frame label
const char *MyStone::CMD_LABEL = "\"label\"";
// Frame text
const char *MyStone::CMD_TEXT = ",\"text\":";
// Frame radio_button
const char *MyStone::CMD_RADIO_BUTTON = "\"radio_button\"";
// Frame widget
const char *MyStone::CMD_WIDGET_TYPE = "\"widget\"";

/*----------------- KEYS & VALUES-----------------*/
// Frame value
const char *MyStone::CMD_VALUE = ",\"value\":";
// Frame windget
const char *MyStone::CMD_WIDGET_KEY = ",\"widget\":";
// Frame enable
const char *MyStone::CMD_ENABLE = ",\"enable\":";
// Frame visible
const char *MyStone::CMD_VISIBLE = ",\"visible\":";

/*----------------- OTHERS -----------------*/
// Frame guillements
const char *MyStone::CMD_QUOTE = "\"";
/**
 *
 * Constructeur de MyStone
 * @param speed Vitesse du protocole Serie
 * @param config Configuration du port Serie
 * @param rxd Broche RXD du protocole Serie
 * @param txd Broche TXD du protocole Serie
 */
MyStone::MyStone(int speed, uint32_t config, int rxd, int txd)
{
  mySerial = new MySerial(speed, config, rxd, txd);
};

/**
 * Destructeur de la classe MyStone
 */
MyStone::~MyStone()
{
  delete mySerial;
};

/**
 * getValidsDatasIdExists()
 * Méthode qui permet de trouver les données ,si données il y a
 *
 * @version 1.2 Adapatation de la structure en fonction du retour du Stone (pour les edits)
 *
 * @return datasRead : Retourne la structure datasRead
 */
datasRead MyStone::getValidsDatasIfExists()
{
  int bytes;
  datasRead returnedDatasRead;

  // Initialisation
  returnedDatasRead.id = 0; // 0 : Pas de données
  returnedDatasRead.line[0] = 0x00;
  returnedDatasRead.command[0] = 0x00;
  returnedDatasRead.data[0] = 0x00;
  returnedDatasRead.nameEdt[0] = 0x00;
  returnedDatasRead.valueEdt[0] = 0x00;
  returnedDatasRead.keyValue = 0;

  // Recherche de données valide
  if (!mySerial->isAvailable())
    return (returnedDatasRead);
  // Décapsulation de l'en-tete
  if (!findHeader())
  {
    return (returnedDatasRead);
  }

  int commande, longeur;
  char data[2048];
  // Lecture des octets pour la commande
  commande = getCMDorLEN();
  // Lecture des octets pour la longueur des données en octect
  longeur = getCMDorLEN();
  // On vérifie s'il y a une ou des erreurs
  if (longeur == 0 || commande == 0)
  {
    return (returnedDatasRead);
  }
  // Lecture des données
  bytes = mySerial->readIt((char *)data, longeur);

  // Lecture du pied de capsule
  if (readTail() == 0)
  {
    return (returnedDatasRead);
  }
  returnedDatasRead.id = commande;

  char nom[50] = {0};
  char valeur[50] = {0};
  int pos, j, i = 0;
  // Traitement des données en fonction de sa provenant
  switch (commande)
  {
  case 0x1070: // EDIT -> retour d'un text saisie sur un edit

    for (j = 0; data[j] != ':' && j < sizeof(data); j++)
    {
    }
    pos = j;

    for (i = 1; i < pos - 1 && i < sizeof(nom) - 1; i++)
    {
      nom[i - 1] = data[i];
    }
    nom[i] = '\0';

    for (j = 0, i = pos + 1; i < longeur && j < sizeof(valeur) - 1; i++, j++)
    {
      valeur[j] = data[i];
    }
    valeur[j] = '\0';
    // Serial.println(nom);
    // Serial.println(valeur);

    strcpy(returnedDatasRead.nameEdt, nom);
    strcpy(returnedDatasRead.valueEdt, valeur);
    break;

  default: // Autres
    int keyValue = (int)data[longeur - 1];
    data[longeur - 1] = 0x00;

    strcpy(returnedDatasRead.data, data);
    returnedDatasRead.keyValue = keyValue;
    break;
  }

  return (returnedDatasRead);
};

/**
 * findHeader
 * Permet de trouver l'en-tête d'une commande retourné par l'écran Stone
 *
 * @return l'existance ou non de l'en-tête (header)
 */
bool MyStone::findHeader()
{
  char data[1];  // Octet pour 1 caractère ASCII
  int index = 0; // Index, nombre de caractères trouvés à la suite
  int byteRead;  // 0 ou moins si octet introuvable

  int longeurHeader = strlen(CMD_HEADER);

  while (index < longeurHeader) // On doit trouver 3 caractères (ST<), c'est notre header
  {
    byteRead = mySerial->readIt(data, 1);

    if (byteRead <= 0)
    {
      return false; // Pas d'octet, donc rien à chercher
    }
    // Vérifier si le caractère correspond au header
    index = (data[0] == CMD_HEADER[index]) ? index + 1 : 0;
  }
  // Si on sort de la boucle, cela signifie que tous les caractères ont été trouvés
  return true;
};

/**
 * getCMDorLEN
 *
 * @brief Méthode permet de lire deux octets, on l'utilise pour la commande et la longueur des données
 *
 * La commande et la longueur des données sont stockées sur deux octets chacun
 * Donc la méthode est la même pour les deux, on différencit juste sont appel
 *
 * @return -1 si erreur, sinon valeur hexadécimal de la strucuture
 */
unsigned short MyStone::getCMDorLEN()
{
  struct
  {
    union
    // Hyper important, l'union permet de mettre l'ensemble des données de ce dernier dans un même emplacement mémoire
    // Impératif pour le swap et la convergence en hexadécimal!!!
    {
      unsigned short hexaShort;
      struct
      {
        char char1;
        char char2;
      } charValues;
    } shortDataCMDorLEN;
  } shortData;

  // Lecture des 4 octets
  int bytes = mySerial->readIt(&shortData.shortDataCMDorLEN.charValues.char1, 2);

  // Vérification si la lecture a réussi
  if (bytes != 2)
  {
    return 0; // Erreur
  }

  // Données inversé parce que Ordre des Octets (Endianness)
  // Ici Big-endian : L'octet le plus significatif (MSB) est stocké en premier.
  // On le replace dans l'ordre que nous souhaitons
  std::swap(shortData.shortDataCMDorLEN.charValues.char2, shortData.shortDataCMDorLEN.charValues.char1);

  return shortData.shortDataCMDorLEN.hexaShort;
};

/**
 * readTail
 * Permet de lire le TAIL de la commande
 *
 * @version 1.0
 * @note Mettre la vérification CRC dans une prochaine version
 * @return bool (false si il y a une erreur)
 */
bool MyStone::readTail()
{
  // Lecture des données suivantes : TAIL (3 char ">ET") et CRC (Hexa16)
  char TailDatas[4];
  int bytes = mySerial->readIt(TailDatas, 5);
  // On trouve et on verifie le TAIL
  if ((bytes != (strlen(TailDatas))) ||
      (TailDatas[0] != CMD_TAIL[0]) ||
      (TailDatas[1] != CMD_TAIL[1]) ||
      (TailDatas[2] != CMD_TAIL[2]))
  {
    return (false);
  }
  return (true);

  // Vérifier le CRC prochainement..
};

/**
 * Methode generateCmd
 *
 * Génère une commande adaptative à envoyer à l'écran Stone.
 *
 * @date Création : 21/03/2025
 *
 * @param cmdCode Le code de la commande à envoyer.
 * @param type Le type de l'élément à modifier.
 * @param widget Le nom de l'élément à modifier.
 * @param key La clé de l'élément à modifier.
 * @param value La valeur à attribuer à l'élément.
 *
 * @return La commande générée.
 */
String MyStone::generateCmd(const char *cmdCode, const char *type, const char *widget, const char *key, const char *value)
{
  // Debug
  // Serial.println("Nouvelle commende :");
  char cmdFormat[1024];
  char valuePart[256] = "";

  // Gérer la valeur en fonction de son type
  if (value != NULL)
  {
    if (strcmp(value, "true") == 0 || strcmp(value, "false") == 0)
    {
      // Ajout de la valeur sans guillemets
      sprintf(valuePart, "%s", value);
    }
    else
    {
      // Ajout de la valeur avec guillemets
      sprintf(valuePart, "%s%s%s", CMD_QUOTE, value, CMD_QUOTE);
    }
  }

  // Structure et construction de la commande
  sprintf(cmdFormat, "%s%s%s%s%s%s%s%s%s%s%s",
          CMD_BEGIN,
          cmdCode,
          CMD_TYPE,
          type,
          CMD_WIDGET_KEY,
          CMD_QUOTE,
          widget,
          CMD_QUOTE,
          key,
          valuePart,
          CMD_END);

  // Debug
  // Serial.println(cmdFormat);
  // Serial.println("");
  return String(cmdFormat);
}

/**
 * Methode getEditTextValue
 *
 * Récupère la valeur d'un champ de texte éditable.
 * Cette fonction envoie une cmde pour obtenir le contenu d'un champ de texte spécifique.
 *
 * @date Dernière mise à jour : 21/03/2025
 *
 * @param editName Le nom de l'élément éditable dont la valeur est à récupérer.
 */
void MyStone::getEditTextValue(const char *editName)
{
  // Générer la commande en utilisant generateCmd
  String command = generateCmd(CMD_GET_TEXT, CMD_EDIT, editName, "", NULL);
  if (mySerial)
    mySerial->writeIt(command.c_str());
}

/**
 * Methode setTextValue
 *
 * Définit le texte d'une étiquette (label).
 * Cette fonction envoie une commande pour modifier le texte affiché par une étiquette.
 *
 * @date Dernière mise à jour : 21/03/2025
 *
 * @param labelName Le nom de l'étiquette.
 * @param text Le texte à afficher dans l'étiquette.
 */
void MyStone::setTextLabel(const char *labelName, const char *text)
{
  String command = generateCmd(CMD_SET_TEXT, CMD_LABEL, labelName, CMD_TEXT, text);
  if (mySerial)
    mySerial->writeIt(command.c_str());
}

/**
 * Methode setTextButton
 *
 * Définit le texte d'un bouton.
 * Cette fonction envoie une commande pour modifier le texte affiché sur un bouton.
 *
 * @date Dernière mise à jour : 21/03/2025
 *
 * @param buttonName Le nom du bouton.
 * @param text Le texte à afficher sur le bouton.
 */
void MyStone::setTextButton(const char *buttonName, const char *text)
{
  String command = generateCmd(CMD_SET_TEXT, CMD_BUTTON, buttonName, CMD_TEXT, text);
  if (mySerial)
    mySerial->writeIt(command.c_str());
}

/**
 * Methode setTipsEdit
 *
 * Définit l'astuce d'un champ éditable.
 * Cette fonction envoie une commande pour attribuer une astuce à un champ éditable.
 *
 * @date Dernière mise à jour : 21/03/2025
 *
 * @param editName Le nom de l'élément éditable.
 * @param tips Le texte de l'astuce à afficher.
 */
void MyStone::setTipsEdit(const char *editName, const char *tips)
{
  String command = generateCmd(CMD_SET_TEXT, CMD_EDIT, editName, CMD_TEXT, tips);
  if (mySerial)
    mySerial->writeIt(command.c_str());
}

/**
 * Methode loadView
 *
 * Charge les fenêtres, les popup, les overlay...
 * Cette fonction envoie une commande pour changer la page affichée.
 * Si aucun nom de page n'est spécifié, elle revient à la page d'accueil.
 *
 * @date Dernière mise à jour : 21/03/2025
 *
 * @param pageName Le nom de la page à afficher. Par défaut : "home_page".
 */
void MyStone::loadView(const char *pageName)
{
  pageName = strlen(pageName) ? pageName : "home_page";
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s",
          CMD_BEGIN,
          CMD_OPEN_WIN,
          CMD_WIDGET_KEY,
          CMD_QUOTE,
          pageName,
          CMD_QUOTE,
          CMD_END);
  if (mySerial)
    mySerial->writeIt(cmdFormat2);
}

/**
 * setRadioButtonTrue
 *
 * Active un bouton radio.
 * Cette fonction envoie une commande pour définir un bouton radio comme étant sélectionné.
 *
 * @date Dernière mise à jour : 21/03/2025
 *
 * @param radioButtonName Le nom du bouton radio à activer.
 */
void MyStone::setRadioButtonTrue(const char *radioButtonName)
{
  String command = generateCmd(CMD_SET_VALUE, CMD_RADIO_BUTTON, radioButtonName, CMD_VALUE, "true");
  if (mySerial)
    mySerial->writeIt(command.c_str());
}

/**
 * setEnable
 *
 * Rend actif ou inactif un élément ,"widget", de l'écran Stone.
 *
 * @date Création : 21/03/2025
 *
 * @param widget Le nom de l'élément à activer ou désactiver.
 * @param enable true pour activer, false pour désactiver.
 */
void MyStone::setEnable(const char *widget, bool enable)
{
  String command = generateCmd(CMD_SET_ENABLE, "\"widget\"", widget, CMD_ENABLE, enable ? "true" : "false");
  if (mySerial)
    mySerial->writeIt(command.c_str());
}

/**
 * setVisible
 *
 * Rend visible ou invisible un élément ,"widget", de l'écran Stone.
 *
 * @date Création : 21/03/2025
 *
 * @param widget Le nom de l'élément.
 * @param enable true pour visible, false pour invisible.
 */
void MyStone::setVisible(const char *widget, bool enable)
{
  String command = generateCmd(CMD_SET_VISIBLE, CMD_WIDGET_TYPE, widget, CMD_VISIBLE, enable ? "true" : "false");
  if (mySerial)
    mySerial->writeIt(command.c_str());
}
