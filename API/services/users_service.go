package services

import (
	"errors"
	"waiter-assist/models/daos"
	"waiter-assist/models/dtos"
	"waiter-assist/repositories"

	"golang.org/x/crypto/bcrypt"
)

func HashPassword(password *string) error {

	if password == nil {
		return errors.New("le pointeur de mot de passe est nil")
	}

	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(*password), bcrypt.DefaultCost)
	if err != nil {
		return err
	}

	*password = string(hashedPassword)
	return nil
}

func VerifyCreateUserJSON(userDTO *dtos.UserDto) string {
	switch {
	case userDTO.FirstName == "":
		return "Le prénom est obligatoire"
	case userDTO.LastName == "":
		return "Le nom est obligatoire"
	case userDTO.Email == "":
		return "L'email est obligatoire"
	case userDTO.Password == "":
		return "Le mot de passe est obligatoire"
	}
	return ""

}

func CreateUser(userDto *dtos.UserDto) (*dtos.UserDtoResponse, error) {

	user := &daos.User{
		FirstName: userDto.FirstName,
		LastName:  userDto.LastName,
		Email:     userDto.Email,
		Password:  userDto.Password,
	}

	err := repositories.CreateUser(user)
	if err != nil {
		return nil, err
	}

	return &dtos.UserDtoResponse{
		FirstName: user.FirstName,
		LastName:  user.LastName,
		Email:     user.Email,
	}, nil
}
