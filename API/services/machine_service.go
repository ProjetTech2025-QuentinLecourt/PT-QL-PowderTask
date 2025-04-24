package services

import (
	"waiter-assist/models/dtos"
	"waiter-assist/repositories"

	"github.com/jinzhu/copier"
)

func GetAllMachines() ([]*dtos.ScaleDto, error) {
	machines, err := repositories.GetAllMachines()
	if err != nil {
		return nil, err
	}

	var machinesDTOs []*dtos.ScaleDto
	for _, machine := range machines {
		machineDto := &dtos.ScaleDto{}
		err = copier.Copy(machineDto, machine)
		machinesDTOs = append(machinesDTOs, machineDto)
	}

	return machinesDTOs, err
}

func GetUserMachines(userId uint) ([]*dtos.ScaleDto, error) {
	machines, err := repositories.GetUserMachines(userId)
	if err != nil {
		return nil, err
	}

	var machinesDTOs []*dtos.ScaleDto
	for _, machine := range machines {
		machineDto := &dtos.ScaleDto{}
		err = copier.Copy(machineDto, machine)
		machinesDTOs = append(machinesDTOs, machineDto)
	}

	return machinesDTOs, err
}

func GetUsersByMachineId(machineId uint) ([]*dtos.UserDtoResponseForList, error) {
	users, err := repositories.GetUsersByMachineId(machineId)
	if err != nil {
		return nil, err
	}

	var usersDTOs []*dtos.UserDtoResponseForList
	for _, user := range users {
		userDto := &dtos.UserDtoResponseForList{}
		err = copier.Copy(userDto, user)
		usersDTOs = append(usersDTOs, userDto)
	}

	return usersDTOs, err

}
