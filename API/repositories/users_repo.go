package repositories

import (
	"waiter-assist/database"
	"waiter-assist/models/daos"
)

func CreateUser(user *daos.User) error {
	err := database.DB.Create(&user).Error
	if err != nil {
		return err
	}

	return nil
}

func GetUserByEmail(email string) (*daos.User, error) {
	var user daos.User
	err := database.DB.Where("email = ?", email).First(&user).Error
	if err != nil {
		return nil, err
	}

	return &user, nil
}
