package controllers

import (
	"net/http"
	"strconv"
	"waiter-assist/models/dtos"
	"waiter-assist/services"

	"github.com/gin-gonic/gin"
)

func getValidatedEmail(c *gin.Context) (string, *gin.H) {
	email, exists := c.Get("email")
	if !exists {
		return "", &gin.H{
			"error":   "Authentification requise",
			"details": "Email manquant dans le contexte",
		}
	}

	emailStr, ok := email.(string)
	if !ok {
		return "", &gin.H{
			"error":   "Format d'email invalide",
			"details": "L'email doit être une chaîne de caractères",
		}
	}

	return emailStr, nil
}

func GetMachines(c *gin.Context) {
	email, errResponse := getValidatedEmail(c)
	if errResponse != nil {
		c.JSON(http.StatusUnauthorized, errResponse)
		return
	}

	user, err := services.GetUserByEmail(email)
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

func GetMachineUsers(c *gin.Context) {
	id := c.Param("id")
	idUnit, err := strconv.ParseUint(id, 10, 32) // Changez de 16 à 32 bits
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error":   "ID invalide",
			"details": "L'ID doit être un nombre entier positif",
		})
		return
	}

	usersList, err := services.GetUsersByMachineId(uint(idUnit)) // Conversion en uint
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   "Erreur lors de la récupération des utilisateurs",
			"details": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"users": usersList})
}
