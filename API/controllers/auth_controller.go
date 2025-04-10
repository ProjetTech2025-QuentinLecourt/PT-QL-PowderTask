package controllers

import (
	"net/http"
	"waiter-assist/models/dtos"
	"waiter-assist/services"

	"github.com/gin-gonic/gin"
)

func Login(c *gin.Context) {
	var userDtoLogin dtos.UserDtoLogin

	err := c.ShouldBindJSON(&userDtoLogin)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	messageErr := services.VerifyLoginJSON(&userDtoLogin)
	if messageErr != "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": messageErr})
		return
	}

	err = services.AuthenticateUser(userDtoLogin.Email, userDtoLogin.Password)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Authentification refusée"})
		return
	}

	user, err := services.GetUserByEmail(userDtoLogin.Email)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   "Erreur lors de la récupération de l'utilisateur",
			"details": err.Error(),
		})
		return
	}

	if user.Job == "" {
		c.JSON(http.StatusForbidden, gin.H{
			"error":   "Accès refusé",
			"details": "Le rôle de l'utilisateur n'est pas défini",
		})
		return
	}

	bearerToken, err := services.GenereateToken(user.FirstName, userDtoLogin.Email, user.Job)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Impossible de générer le token"})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"message":     "Authentification réussie",
		"bearerToken": bearerToken,
	})

}
