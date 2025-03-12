package midllewares

import (
	"log"
	"net/http"
	"os"
	"strings"
	"waiter-assist/models"

	"github.com/dgrijalva/jwt-go"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

// JWT_SECRET est la clé secrète utilisée pour signer et vérifier les JWT
var JWT_SECRET []byte

// init() est appelée automatiquement avant main()
func init() {
	// Charge les variables d'environnement depuis un fichier .env
	if err := godotenv.Load(); err != nil {
		log.Println("Aucun fichier .env trouvé, utilisation des variables d'environnement système")
	}

	// Initialise JWT_SECRET avec la valeur de la variable d'environnement
	JWT_SECRET = []byte(os.Getenv("SECRET_KEY"))
}

// Fonction privée pour renvoyer l'erreur de non autorisation et annulée la requête suivante
func unauthorizedReturn(c *gin.Context, message string) {
	c.JSON(http.StatusUnauthorized, gin.H{"error": message})
	c.Abort()
}

// AuthMiddleware est le middleware pour vérifier le jwt
func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			unauthorizedReturn(c, "Token manquant")
			return
		}

		// Vérifie que le token utilise le schéma "Bearer"
		tokenParts := strings.Split(authHeader, " ")
		if len(tokenParts) != 2 || tokenParts[0] != "Bearer" {
			unauthorizedReturn(c, "Format de token invalide")
			return
		}

		jwtToken := tokenParts[1]

		claims := &models.Claims{}

		token, err := jwt.ParseWithClaims(jwtToken, claims, func(token *jwt.Token) (interface{}, error) {
			return JWT_SECRET, nil
		})
		if err != nil {
			unauthorizedReturn(c, "Le token comporte une ou plusieurs erreurs")
			return
		}
		if !token.Valid {
			unauthorizedReturn(c, "Token invalide")
			return
		}

		// Stocke l'email dans le contexte pour les autres fonctionnalités
		c.Set("email", claims.Email)
		c.Next()
	}
}
