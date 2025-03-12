
#ifndef STRUCTS_H
#define STRUCTS_H
// Structure local utilisés pour garder les informations lues de l'écran

struct datasRead
{
    int id; // Si 0 alors il n'y a pas de données, <0 Numero commande non traitée, >1 Numero de la commande traité
    char command[80];
    char data[80];
    char nameEdt[50];
    char valueEdt[50];
    int keyValue;
    char line[2048];
};
#endif