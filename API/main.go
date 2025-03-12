package main

import (
	"log"
	"os"
	"waiter-assist/database"
	"waiter-assist/routes"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {

	if err := godotenv.Load(); err != nil {
		log.Println("Aucun fichier .env trouvé, utilisation des variables d'environnement système")
	}
	if os.Getenv("ENV") == "PROD" {
		gin.SetMode(gin.ReleaseMode) // Désactive les logs de débogage en production
	}

	r := gin.Default()

	routes.RegisterRoutes(r)

	port := os.Getenv("PORT")
	if port == "" {
		port = "2990" // Port par défaut
	}

	database.Connect()

	r.Run(":" + port)
}
