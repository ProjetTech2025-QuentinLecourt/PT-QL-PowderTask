# API LLIO

Une API développée en Go avec le framework Gin-Gonic.

---

## Structure du projet

```plaintext
API/
├── controllers/   # Logique des routes
├── database/      # Connexion et configuration de la base de données
|__ auth/          # Configuration de l'authentification 
├── main.go        # Point d'entrée de l'application
├── middleware/    # Middlewares
├── models/        # Modèles de données
│   ├── DAOs/      # Objets d'accès aux données
│   └── DTOs/      # Objets de transfert de données
├── repositories/  # Gestion des accès aux données
├── routes/        # Définition des routes
├── services/      # Logique métier
├── tests/         # Tests unitaires et d'intégration
└── useful/        # Fonctions utilitaires
```
# Golang
1. Installation de la derniere version stable de go sur golang.org
    (go version go1.23.5 windows/amd64)
   
2. Instalation des modules GO
    go mod init llio-api

# Gin
- Installation des modules gin-gonic
   go get -u github.com/gin-gonic/gin

# Base de données
1. Installation des modules de l'ORM pour l'utilisation d'une BD MariaDB
   go get -u gorm.io/gorm
   go get -u gorm.io/driver/mysql

2. Créer une base de données SQL

# Authentification
1. Installation des modules d'authentification et de session
   go get github.com/markbates/goth
   go get github.com/gorilla/sessions

2. Import du provider pour l'authentification avec azuread
   import (
      "github.com/markbates/goth/providers/azureadv2"
   )


# Variables d'environnement
1. Installation du module qui nous permet d'utiliser les variables d'environnement
   go get github.com/joho/godotenv

2. Structure
   Voici la structure de fichier .env pour que l'API puisse fonctionner:
   ```plaintext

    DB_NAME_TEST: [nom de la BD]
    DB_HOST:[adresse de la bd]
    DB_USER: [identifiant user BD]
    DB_USER_PASSWORD: [mot de passe user BD]
    DB_PORT:[port de la BD]

    JWT_SECRET= [clé secret]

    ENV: [environnement d'execution]
    PORT: [port de l'api]
   ```
# Test
## Installation de la librairie de test pour du golang : testify
Installation de la librairie de base:
go get -u github.com/stretchr/testify


# Démarrage du serveur
go run main.go

# Collection ThunderClient
``` 
    thunder-collection_PowderTaskAPI.json
```
