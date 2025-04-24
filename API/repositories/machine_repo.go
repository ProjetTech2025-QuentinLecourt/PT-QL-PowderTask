package repositories

import (
	"waiter-assist/database"
	"waiter-assist/models/daos"
)

func GetAllMachines() ([]*daos.Scale, error) {
	var machines []*daos.Scale
	err := database.DB.Find(&machines).Error

	return machines, err
}

func GetUserMachines(userId uint) ([]*daos.Scale, error) {
	var scales []*daos.Scale

	err := database.DB.
		Joins("JOIN users_scales ON scales.id = users_scales.scales_id").
		Where("users_scales.users_id = ?", userId).
		Find(&scales).Error

	if err != nil {
		return nil, err
	}

	return scales, nil
}

func GetUsersByMachineId(machineId uint) ([]*daos.User, error) {
	var users []*daos.User

	err := database.DB.
		Joins("JOIN users_scales ON users_scales.users_id = users.id").
		Where("users_scales.scales_id = ?", machineId).
		Find(&users).
		Error

	if err != nil {
		return nil, err
	}

	return users, nil
}
