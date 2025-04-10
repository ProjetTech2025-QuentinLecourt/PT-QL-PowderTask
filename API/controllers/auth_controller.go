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

	// Get l'utilisateur ici

	bearerToken, err := services.GenereateToken("Test", userDtoLogin.Email, "CE")
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Impossible de générer le token"})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"message":     "Authentification réussie",
		"bearerToken": bearerToken,
	})

}
