package services

import (
	"waiter-assist/models/dtos"
	"waiter-assist/repositories"

	"golang.org/x/crypto/bcrypt"
)

func VerifyLoginJSON(userDtoLogin *dtos.UserDtoLogin) string {
	switch {
	case userDtoLogin.Email == "":
		return "L'email est obligatoire"
	case userDtoLogin.Password == "":
		return "Le mot de passe est obligatoire"
	}
	return ""
}

func AuthenticateUser(email string, password string) error {

	user, err := repositories.GetUserByEmail(email)
	if err != nil {
		return err
	}

	return bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password))
}
