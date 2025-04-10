package services

import (
	"log"
	"os"
	"time"
	"waiter-assist/models"

	"github.com/dgrijalva/jwt-go"
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

func GenereateToken(firstname string, email string, job string) (string, error) {
	expirationTime := time.Now().Add(72 * time.Hour)

	claims := &models.Claims{
		FirstName: firstname,
		Email:     email,
		Job:       job,
		StandardClaims: jwt.StandardClaims{
			ExpiresAt: expirationTime.Unix(),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)

	responseToken, err := token.SignedString(JWT_SECRET)
	if err != nil {
		return "", err
	}

	return responseToken, nil
}
