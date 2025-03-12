package tests

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"waiter-assist/controllers"
	"waiter-assist/database"
	"waiter-assist/models/daos"
	"waiter-assist/models/dtos"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func TestCreateUser(t *testing.T) {
	ChangeCurrentDiretory()

	database.Connect()

	router := gin.Default()
	router.POST("/users", controllers.CreateUser)

	user := struct {
		FirstName string `json:"first_name"`
		LastName  string `json:"last_name"`
		Email     string `json:"email"`
		Password  string `json:"password"`
	}{
		FirstName: "John",
		LastName:  "Doe",
		Email:     "testing@gmail.com",
		Password:  "password",
	}

	jsonValue, _ := json.Marshal(user)

	req, _ := http.NewRequest("POST", "/users", bytes.NewBuffer(jsonValue))
	req.Header.Set("Content-Type", "application/json")

	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusCreated, w.Code)

	var responseUser struct {
		Message string               `json:"message"`
		User    dtos.UserDtoResponse `json:"user"`
	}

	err := json.Unmarshal(w.Body.Bytes(), &responseUser)
	assert.NoError(t, err)
	assert.Equal(t, "Utilisateur créé avec succès", responseUser.Message)
	assert.Equal(t, user.FirstName, responseUser.User.FirstName)
	assert.Equal(t, user.LastName, responseUser.User.LastName)
	assert.Equal(t, user.Email, responseUser.User.Email)

	var createdUser daos.User
	errDB := database.DB.Where("email = ?", user.Email).First(&createdUser).Error
	assert.NoError(t, errDB)
	assert.Equal(t, user.FirstName, createdUser.FirstName)
	assert.Equal(t, user.LastName, createdUser.LastName)
	assert.Equal(t, user.Email, createdUser.Email)
	assert.NotEqual(t, user.Password, createdUser.Password)

	errDB = database.DB.Unscoped().Delete(&createdUser).Error
	assert.NoError(t, errDB)

}
