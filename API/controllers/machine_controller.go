package controllers

import (
	"net/http"
	"waiter-assist/models/dtos"
	"waiter-assist/services"

	"github.com/gin-gonic/gin"
)

func GetMachines(c *gin.Context) {
	email, exists := c.Get("email")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{
			"error":   "Authentification requise",
			"details": "Email manquant dans le contexte",
		})
		return
	}

	emailStr, ok := email.(string)
	if !ok {
		c.JSON(http.StatusBadRequest, gin.H{
			"error":   "Format d'email invalide",
			"details": "L'email doit être une chaîne de caractères",
		})
		return
	}

	user, err := services.GetUserByEmail(emailStr)
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

	var machines []*dtos.ScaleDto
	switch user.Job {
	case "CE":
		machines, err = services.GetAllMachines()
	case "P", "OP":
		machines, err = services.GetUserMachines(user.Id)
	default:
		c.JSON(http.StatusForbidden, gin.H{
			"error":   "Accès refusé",
			"details": "Rôle utilisateur non reconnu: " + user.Job,
		})
		return
	}

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   "Erreur lors de la récupération des machines",
			"details": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"message": "Liste des machines récupérée avec succès",
		"data": gin.H{
			"scales": machines,
		},
	})
}
