package controllers

import (
	"net/http"
	"waiter-assist/models/dtos"
	"waiter-assist/services"

	"github.com/gin-gonic/gin"
)

func CreateUser(c *gin.Context) {
	var userDto dtos.UserDto
	err := c.ShouldBindJSON(&userDto)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	messageErr := services.VerifyCreateUserJSON(&userDto)
	if messageErr != "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": messageErr})
		return
	}

	if err := services.HashPassword(&userDto.Password); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	userResponse, err := services.CreateUser(&userDto)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, gin.H{
		"message": "Utilisateur créé avec succès",
		"user":    userResponse,
	})
}
