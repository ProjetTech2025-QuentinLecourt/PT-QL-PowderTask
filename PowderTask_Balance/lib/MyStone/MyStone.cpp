/**
 * Gestion des communications avec l'écran Stone
 *
 * @file MyStone.cpp
 * @author Lecourt Quentin
 * @cite Inspiré de MyStone par Alain Dubé
 * @brief Methode de la classe MyStone
 * @version 1.4
 * @date Création : 01/10/2024
 * @date Dernière mise à jour : Création : 10/10/2024
 */
#include "MyStone.h"

/**
 * Constante static pour la instruction de retour Serie du Stone
 */
// Frame header
const char *MyStone::COMMAND_HEADER = "ST<";
// Frame tail
const char *MyStone::COMMAND_TAIL = ">ET";

// Frame debut d'une commande
const char *MyStone::COMMAND_BEGIN = "ST<{\"cmd_code\":";
// Frame fin d'une commande
const char *MyStone::COMMAND_END = "}>ET";

// Frame get_value
const char *MyStone::COMMAND_GET_TEXT = "\"get_text\"";
// Frame set_value
const char *MyStone::COMMAND_SET_VALUE = "\"set_value\"";
// Frame set_text pour commande
const char *MyStone::COMMAND_SET_TEXT = "\"set_text\"";
// Frame open_win pour commande
const char *MyStone::COMMAND_OPEN_WIN = "\"open_win\"";
// Frame back_win pour commande
const char *MyStone::COMMAND_BACK_WIN = "\"back_win\"";

// Frame type pour commande
const char *MyStone::COMMAND_TYPE = ",\"type\":";
// Frame edit pour commande
const char *MyStone::COMMAND_EDIT = "\"edit\"";
// Frame button
const char *MyStone::COMMAND_BUTTON = "\"button\"";
// Frame label pour commande
const char *MyStone::COMMAND_LABEL = "\"label\"";
// Frame text pour commande
const char *MyStone::COMMAND_TEXT = ",\"text\":";
// Frame radio_button
const char *MyStone::COMMAND_RADIO_BUTTON = "\"radio_button\"";

// Frame " pour commande
const char *MyStone::COMMAND_QUOTE = "\"";
// Frame value
const char *MyStone::COMMAND_VALUE = ",\"value\":";
// Frame windget pour commande
const char *MyStone::COMMAND_WINDGET = ",\"widget\":";

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
MyStone::~MyStone() {};

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
    Serial.println(nom);
    Serial.println(valeur);

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

  int longeurHeader = strlen(COMMAND_HEADER);

  while (index < longeurHeader) // On doit trouver 3 caractères (ST<), c'est notre header
  {
    byteRead = mySerial->readIt(data, 1);

    if (byteRead <= 0)
    {
      return false; // Pas d'octet, donc rien à chercher
    }
    // Vérifier si le caractère correspond au header
    index = (data[0] == COMMAND_HEADER[index]) ? index + 1 : 0;
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
      (TailDatas[0] != COMMAND_TAIL[0]) ||
      (TailDatas[1] != COMMAND_TAIL[1]) ||
      (TailDatas[2] != COMMAND_TAIL[2]))
  {
    return (false);
  }
  return (true);

  // Vérifier le CRC prochainement..
};

/**
 * Methode getEditTextValue
 * 
 * Récupère la valeur d'un champ de texte éditable.
 * Cette fonction envoie une commande pour obtenir le contenu d'un champ de texte spécifique.
 * 
 * @param editName Le nom de l'élément éditable dont la valeur est à récupérer.
 */
void MyStone::getEditTextValue(const char *editName)
{
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s%s%s%s",
          COMMAND_BEGIN,

          COMMAND_GET_TEXT,

          COMMAND_TYPE,
          COMMAND_EDIT,

          COMMAND_WINDGET,
          COMMAND_QUOTE,
          editName,
          COMMAND_QUOTE,
          COMMAND_END);

  if (mySerial)
    mySerial->writeIt(cmdFormat2);
};

/**
 * Methode setTextValue
 * 
 * Définit le texte d'une étiquette (label).
 * Cette fonction envoie une commande pour modifier le texte affiché par une étiquette.
 * 
 * @param labelName Le nom de l'étiquette.
 * @param text Le texte à afficher dans l'étiquette.
 */
void MyStone::setTextLabel(const char *labelName, const char *text)
{
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s%s%s%s%s%s%s",
          COMMAND_BEGIN,

          COMMAND_SET_TEXT,

          COMMAND_TYPE,
          COMMAND_LABEL,

          COMMAND_WINDGET,
          COMMAND_QUOTE,
          labelName,
          COMMAND_QUOTE,

          COMMAND_TEXT,
          COMMAND_QUOTE,
          text,
          COMMAND_QUOTE,

          COMMAND_END);

  if (mySerial)
    mySerial->writeIt(cmdFormat2);
};

/**
 * Methode setTextButton
 * 
 * Définit le texte d'un bouton.
 * Cette fonction envoie une commande pour modifier le texte affiché sur un bouton.
 * 
 * @param buttonName Le nom du bouton.
 * @param text Le texte à afficher sur le bouton.
 */
void MyStone::setTextButton(const char *buttonName, const char *text)
{
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s%s%s%s%s%s%s",
          COMMAND_BEGIN,

          COMMAND_SET_TEXT,

          COMMAND_TYPE,
          COMMAND_BUTTON,

          COMMAND_WINDGET,
          COMMAND_QUOTE,
          buttonName,
          COMMAND_QUOTE,

          COMMAND_TEXT,
          COMMAND_QUOTE,
          text,
          COMMAND_QUOTE,

          COMMAND_END);

  if (mySerial)
    mySerial->writeIt(cmdFormat2);
};

/**
 * Methode setTipsEdit
 * 
 * Définit l'astuce d'un champ éditable.
 * Cette fonction envoie une commande pour attribuer une astuce à un champ éditable.
 * 
 * @param editName Le nom de l'élément éditable.
 * @param tips Le texte de l'astuce à afficher.
 */
void MyStone::setTipsEdit(const char *editName, const char *tips)
{
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s%s%s%s%s%s%s",
          COMMAND_BEGIN,

          COMMAND_SET_TEXT,

          COMMAND_TYPE,
          COMMAND_EDIT,

          COMMAND_WINDGET,
          COMMAND_QUOTE,
          editName,
          COMMAND_QUOTE,

          COMMAND_TEXT,
          COMMAND_QUOTE,
          tips,
          COMMAND_QUOTE,

          COMMAND_END);

  if (mySerial)
    mySerial->writeIt(cmdFormat2);
};

/**
 * Methode changePage
 * 
 * Change la page actuelle.
 * Cette fonction envoie une commande pour changer la page affichée. 
 * Si aucun nom de page n'est spécifié, elle revient à la page d'accueil.
 * 
 * @param pageName Le nom de la page à afficher. Par défaut : "home_page".
 */
void MyStone::changePage(const char *pageName)
{
  pageName = strlen(pageName) ? pageName : "home_page";
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s",
          COMMAND_BEGIN,

          COMMAND_OPEN_WIN,

          COMMAND_WINDGET,
          COMMAND_QUOTE,
          pageName,
          COMMAND_QUOTE,

          COMMAND_END);

  if (mySerial)
    mySerial->writeIt(cmdFormat2);
};

/**
 * setRadioButtonTrue
 * 
 * Active un bouton radio.
 * Cette fonction envoie une commande pour définir un bouton radio comme étant sélectionné.
 * 
 * @param radioButtonName Le nom du bouton radio à activer.
 */
void MyStone::setRadioButtonTrue(const char *radioButtonName)
{
  char cmdFormat2[1024];
  sprintf(cmdFormat2, "%s%s%s%s%s%s%s%s%s%s%s",
          COMMAND_BEGIN,

          COMMAND_SET_VALUE,

          COMMAND_TYPE,
          COMMAND_RADIO_BUTTON,

          COMMAND_WINDGET,
          COMMAND_QUOTE,
          radioButtonName,
          COMMAND_QUOTE,

          COMMAND_VALUE,
          "true",

          COMMAND_END);

  if (mySerial)
    mySerial->writeIt(cmdFormat2);
};
